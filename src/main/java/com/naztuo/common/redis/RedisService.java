package com.naztuo.common.redis;

import com.alibaba.fastjson.JSON;
import com.naztuo.common.redis.keys.KeyPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置对象
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {

        String str = beanToString(value);
        if (str == null || str.length() <= 0) {
            return false;
        }
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        int seconds = prefix.expireSeconds();
        if (seconds <= 0) {
            redisTemplate.opsForValue().set(realKey, value);
        } else {
            redisTemplate.opsForValue().set(realKey, value, seconds, TimeUnit.SECONDS);
        }
        return true;

    }

    /**
     * 获取当个对象
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        String str = (String) redisTemplate.opsForValue().get(realKey);
        T t = stringToBean(str, clazz);
        return t;
    }

    public <T> T  execute(DefaultRedisScript<T> redisScript, List<String> keys, Object... args) {
        return (T) redisTemplate.execute(redisScript, keys, args);
    }


    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    public String get(String key) {
        String result = (String) redisTemplate.opsForValue().get(key);
        return result;
    }

    /**
     * 删除
     */
    public boolean delete(KeyPrefix prefix, String key) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        boolean ret = redisTemplate.delete(realKey);
        return ret;

    }


    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }

}
