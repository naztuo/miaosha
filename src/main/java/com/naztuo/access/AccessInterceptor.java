package com.naztuo.access;

import com.alibaba.fastjson.JSON;
import com.naztuo.common.enums.ResultStatus;
import com.naztuo.common.redis.RedisService;
import com.naztuo.common.resultbean.RspMsg;
import com.naztuo.user.bean.MiaoshaUser;
import com.naztuo.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.OutputStream;

import static com.naztuo.common.enums.ResultStatus.ACCESS_LIMIT_REACHED;
import static com.naztuo.common.enums.ResultStatus.SESSION_ERROR;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    /**
     * 限流
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        /**
         * 获取调用 获取主要方法
         */
        if(handler instanceof HandlerMethod) {
            logger.info("打印拦截方法handler ：{} ",handler);
            HandlerMethod hm = (HandlerMethod)handler;
            //方便mybatis 测试
//			if(hm.getMethod().getName().startsWith("test")){
//				return true;
//			}
            MiaoshaUser user = getUser(request, response);
            UserContext.setUser(user);
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit == null) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if(needLogin) {
                if(user == null) {
                    render(response, SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getNickname();
            }else {
                //do nothing
            }
            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if(count  == null) {
                redisService.set(ak, key, 1);
            }else if(count < maxCount) {
                redisService.incr(ak, key);
            }else {
                render(response, ACCESS_LIMIT_REACHED);
                return false;
            }
        }
        return true;
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(UserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, UserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void render(HttpServletResponse response, ResultStatus cm)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str  = JSON.toJSONString(RspMsg.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }
}
