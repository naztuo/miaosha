package com.naztuo.user.service;

import com.naztuo.common.enums.ResultStatus;
import com.naztuo.common.redis.RedisUserKey;
import com.naztuo.common.redis.RedisService;
import com.naztuo.exception.GlobleException;
import com.naztuo.user.bean.LoginVo;
import com.naztuo.user.bean.MiaoshaUser;
import com.naztuo.user.dao.UserDao;
import com.naztuo.util.MD5Utils;
import com.naztuo.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private RedisService redisService;

    @Resource
    private UserDao userDao;


    public BufferedImage createVerifyCodeRegister(String ipAddress) {
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisService.set(RedisUserKey.getVerifyCodeRegister, ipAddress, rnd);
        //输出图片
        return image;
    }

    /**
     * 注册时用的验证码
     *
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCodeRegister(int verifyCode, String ipAddress) {
        Integer codeOld = redisService.get(RedisUserKey.getVerifyCodeRegister, ipAddress, Integer.class);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        redisService.delete(RedisUserKey.getVerifyCodeRegister, ipAddress);
        return true;
    }

    public boolean register(HttpServletResponse response, String userName, String passWord, String salt) {
        MiaoshaUser miaoShaUser = new MiaoshaUser();
        miaoShaUser.setNickname(userName);
        String DBPassWord = MD5Utils.formPassToDBPass(passWord, salt);
        miaoShaUser.setPassword(DBPassWord);
        miaoShaUser.setRegisterDate(new Date());
        miaoShaUser.setSalt(salt);
        miaoShaUser.setNickname(userName);
        try {
            userDao.insertMiaoShaUser(miaoShaUser);
            MiaoshaUser user = userDao.getByNickname(miaoShaUser.getNickname());
            if (user == null) {
                return false;
            }
            //生成cookie 将session返回游览器 分布式session
            String token = UUIDUtil.uuid();
            addCookie(response, token, user);
        } catch (Exception e) {
            LOGGER.error("注册失败", e);
            return false;
        }
        return true;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(RedisUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        //设置有效期
        cookie.setMaxAge(RedisUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    /**
     * + - *
     */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            Integer catch1 = (Integer) engine.eval(exp);
            return catch1.intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void vistorCount(String key) {
        String count = "local num=redis.call('incr',KEYS[1]) return num";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(count, Long.class);
        java.util.List<String> keys = new ArrayList<>();
        keys.add(key);
        redisService.execute(redisScript, keys, null);
    }

    public long getVistorCount(String key) {
        String count = "local num=redis.call('get',KEYS[1]) return num";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(count, Long.class);
        List<String> keys = new ArrayList<>();
        return redisService.execute(redisScript, keys, null);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobleException(ResultStatus.SYSTEM_ERROR);
        }

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        MiaoshaUser user = getByNickName(mobile);
        if (user == null) {
            throw new GlobleException(ResultStatus.MOBILE_NOT_EXIST);
        }

        String dbPass = user.getPassword();
        String saltDb = user.getSalt();
        String calcPass = MD5Utils.formPassToDBPass(password, saltDb);
        if (!calcPass.equals(dbPass)) {
            throw new GlobleException(ResultStatus.PASSWORD_ERROR);
        }
        //生成cookie 将session返回游览器 分布式session
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public String createToken(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobleException(ResultStatus.SYSTEM_ERROR);
        }

        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        MiaoshaUser user = getByNickName(mobile);
        if (user == null) {
            throw new GlobleException(ResultStatus.MOBILE_NOT_EXIST);
        }

        String dbPass = user.getPassword();
        String saltDb = user.getSalt();
        String calcPass = MD5Utils.formPassToDBPass(password, saltDb);
        if (!calcPass.equals(dbPass)) {
            throw new GlobleException(ResultStatus.PASSWORD_ERROR);
        }
        //生成cookie 将session返回游览器 分布式session
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    public MiaoshaUser getByNickName(String nickName) {
        //取缓存
        MiaoshaUser user = redisService.get(RedisUserKey.getByNickName, "" + nickName, MiaoshaUser.class);
        if (user != null) {
            return user;
        }
        //取数据库
        user = userDao.getByNickname(nickName);
        if (user != null) {
            redisService.set(RedisUserKey.getByNickName, "" + nickName, user);
        }
        return user;
    }

}
