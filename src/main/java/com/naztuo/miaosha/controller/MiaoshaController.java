package com.naztuo.miaosha.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.naztuo.access.AccessLimit;
import com.naztuo.common.enums.ResultStatus;
import com.naztuo.common.redis.RedisService;
import com.naztuo.common.redis.keys.GoodsKey;
import com.naztuo.common.resultbean.RspMsg;
import com.naztuo.miaosha.bean.GoodsVo;
import com.naztuo.miaosha.bean.MiaoshaMessage;
import com.naztuo.miaosha.bean.MiaoshaOrder;
import com.naztuo.miaosha.service.GoodsService;
import com.naztuo.miaosha.service.OrderService;
import com.naztuo.mq.MQSender;
import com.naztuo.user.bean.MiaoshaUser;
import com.naztuo.user.service.MiaoshaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.naztuo.common.enums.ResultStatus.*;

@Controller
@RequestMapping("miaosha")
public class MiaoshaController implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(MiaoshaController.class);

    @Autowired
    private MiaoshaService miaoshaService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();


    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public RspMsg getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return RspMsg.error(ResultStatus.USER_NOT_EXIST);
        }
        try {
            BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return RspMsg.success(SUCCESS.getMessage(),image);
        } catch (Exception e) {
            logger.error("生成验证码错误-----goodsId:{}", goodsId, e);
            return RspMsg.error(MIAOSHA_FAIL);
        }
    }

    /**
     * 校验验证码并限制前端点击次数
     * @param request
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RspMsg getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                 @RequestParam("goodsId") long goodsId,
                                 @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null) {
            return RspMsg.error(SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return RspMsg.error(REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return RspMsg.success(SUCCESS.getMessage(), path);
    }

    /**
     * QPS:1306
     * 5000 * 10
     * get　post get 幂等　从服务端获取数据　不会产生影响　　post 对服务端产生变化
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value="/{path}/do_miaosha", method= RequestMethod.POST)
    @ResponseBody
    public RspMsg miaosha(Model model, MiaoshaUser user, @PathVariable("path") String path,
                                        @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return RspMsg.error(SESSION_ERROR);
        }
        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if (!check) {
            return RspMsg.error(REQUEST_ILLEGAL);
        }
        //使用RateLimiter 限流
		RateLimiter rateLimiter = RateLimiter.create(10);
		//判断能否在1秒内得到令牌，如果不能则立即返回false，不会阻塞程序
		if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
			System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
			return RspMsg.error(MIAOSHA_FAIL);
		}
        //是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(Long.valueOf(user.getNickname()), goodsId);
        if (order != null) {
            return RspMsg.error(REPEATE_MIAOSHA);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return RspMsg.error(MIAO_SHA_OVER);
        }
        //预见库存
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return RspMsg.error(MIAO_SHA_OVER);
        }
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setGoodsId(goodsId);
        mm.setUser(user);
        mqSender.sendMiaoshaMessage(mm);
        return RspMsg.success();
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RspMsg miaoshaResult(Model model, MiaoshaUser user,
                                           @RequestParam("goodsId") long goodsId) {
        if (user == null) {
            return RspMsg.error(SESSION_ERROR);
        }
        model.addAttribute("user", user);
        Long miaoshaResult = miaoshaService.getMiaoshaResult(Long.valueOf(user.getNickname()), goodsId);
        return RspMsg.success(SUCCESS.getMessage(),miaoshaResult);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }
}
