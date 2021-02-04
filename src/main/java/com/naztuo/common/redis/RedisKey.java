package com.naztuo.common.redis;

public class RedisKey extends BasePrefix {

    private RedisKey( int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static RedisKey isGoodsOver = new RedisKey(0, "go");
    public static RedisKey getMiaoshaPath = new RedisKey(60, "mp");
    public static RedisKey getMiaoshaVerifyCode = new RedisKey(300, "vc");
    public static RedisKey getVerifyCodeRegister = new RedisKey(300, "register");
}
