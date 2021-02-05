package com.naztuo.common.redis.keys;

public class RedisUserKey extends BasePrefix {

    private RedisUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;
    public static RedisUserKey token = new RedisUserKey(TOKEN_EXPIRE, "tk");
    public static RedisUserKey getByNickName = new RedisUserKey(0, "nickName");
    public static RedisUserKey getVerifyCodeRegister = new RedisUserKey(300, "register");
}
