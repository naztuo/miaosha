package com.naztuo.common.log;

import com.alibaba.fastjson.JSON;
import com.naztuo.common.resultbean.RspMsg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * @author hpjiang 2019年9月25日 上午9:27:56
 */
public class LogHelpler {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogHelpler.class);

    /**
     * 日志开始触发动作
     *
     * @param classMethod
     * @param reqContent
     */
    public static void logStart(String classMethod, String reqContent) {
        String requestId = String.valueOf(UUID.randomUUID());
        MDC.put("requestId", requestId);

        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        String url = request.getRequestURL().toString();// 请求url
        String httpMethod = request.getMethod();// 请求类型POST、GET
        String ip = request.getRemoteAddr();// 访问IP

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("req- {} {} ip:{} args:{} {}", httpMethod, url, ip, reqContent, classMethod);
        } else {
            LOGGER.info("req- {} {} ip:{} args:{} {}", httpMethod, url, ip, StringUtils.substring(reqContent, 0, 200), classMethod);
        }
    }

    /**
     * 日志结束触发动作
     *
     * @param rsp
     * @param startTime
     */
    public static void logEnd(RspMsg rsp, long startTime) {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        String url = request.getRequestURL().toString();// 请求url
        String ip = request.getRemoteAddr();// 访问IP

        long costs = System.currentTimeMillis() - startTime;// 请求耗时

        // 处理完请求，返回内容,打印耗时
        LOGGER.info("rsp- {} ip:{} costs:{}ms", url, ip, costs);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("response:{}", JSON.toJSONString(rsp));
        }

        MDC.clear();
    }
}
