package com.naztuo.miaosha.controller;

import com.naztuo.common.redis.keys.RedisGoodsKey;
import com.naztuo.miaosha.bean.GoodsVo;
import com.naztuo.miaosha.service.GoodsService;
import com.naztuo.user.bean.MiaoshaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class GoodsController extends BaseController {

    @Autowired
    private GoodsService goodsService;

    /**
     * QPS:1267 load:15 mysql
     * 5000 * 10
     * QPS:2884, load:5
     * */
    @RequestMapping(value="/to_list", produces="text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
        model.addAttribute("user", user);
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        return render(request,response,model,"goods_list", RedisGoodsKey.getGoodsList,"");
    }
}
