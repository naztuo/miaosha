package com.naztuo.mq;

import com.naztuo.common.redis.RedisService;
import com.naztuo.miaosha.bean.GoodsVo;
import com.naztuo.miaosha.bean.MiaoshaMessage;
import com.naztuo.miaosha.bean.MiaoshaOrder;
import com.naztuo.miaosha.service.GoodsService;
import com.naztuo.miaosha.service.OrderService;
import com.naztuo.user.bean.MiaoshaUser;
import com.naztuo.user.service.MiaoshaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message) {
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser user = miaoshaMessage.getUser();
        long goodsId = miaoshaMessage.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(Long.valueOf(user.getNickname()), goodsId);
        if(order != null) {
            return;
        }
        //减库存 下订单 写入秒杀订单
        miaoshaService.miaosha(user, goods);
    }
}
