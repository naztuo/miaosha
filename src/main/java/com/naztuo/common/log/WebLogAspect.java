package com.naztuo.common.log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.naztuo.common.resultbean.RspMsg;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.List;


@Aspect
@Component
public class WebLogAspect {

    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    //扫描所有itms下所有controller包下的所有方法
    @Pointcut("execution(public * com.naztuo..*.controller..*.*(..))")
    public void writeLog() {
    }

    /**
     * 切面前操作
     *
     * @param joinPoint
     */
    @Before("writeLog()")
    public void doBefore(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());

        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();//调用类名
        // LogHelpler.logStart(classMethod,JSON.toJSONString(joinPoint.getArgs()));
        // logger.info("joinPoint args:{}",joinPoint.getArgs());
        Object[] args = joinPoint.getArgs();
        List<Object> paramList = Lists.newArrayListWithCapacity(args.length);
        for (Object parameter : args) {
            if (parameter instanceof ServletRequest || parameter instanceof ServletResponse || parameter instanceof MultipartFile) {
                continue;
            }
            paramList.add(parameter);
        }
        LogHelpler.logStart(classMethod, JSON.toJSONString(paramList, SerializerFeature.IgnoreNonFieldGetter));
    }

    /**
     * 切面后操作
     *
     * @param rsp
     */
    @AfterReturning(returning = "rsp", pointcut = "writeLog()")
    public void doAfterReturning(RspMsg rsp) {
        LogHelpler.logEnd(rsp, startTime.get());
    }

}
