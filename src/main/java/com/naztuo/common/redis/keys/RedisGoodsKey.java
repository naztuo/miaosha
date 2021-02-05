package com.naztuo.common.redis.keys;

public class RedisGoodsKey extends BasePrefix {
    public RedisGoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static RedisGoodsKey getGoodsList = new RedisGoodsKey(60, "gl");
    public static RedisGoodsKey getGoodsDetail = new RedisGoodsKey(60, "gd");
    public static RedisGoodsKey getMiaoshaGoodsStock = new RedisGoodsKey(0, "gs");
}
