package com.naztuo.exception;

import com.naztuo.common.enums.ResultStatus;
import com.naztuo.common.resultbean.RspMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 拦截异常
 *
 * @author qiurunze
 */
@ControllerAdvice
@ResponseBody
public class GlobleExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GlobleExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public RspMsg exceptionHandler(HttpServletRequest request, Exception e) {
        if (e instanceof GlobleException) {
            GlobleException ex = (GlobleException) e;
            logger.error(e.getMessage(),e);
            return RspMsg.error(ex.getStatus());
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<ObjectError> errors = ex.getAllErrors();
            ObjectError error = errors.get(0);
            String msg = error.getDefaultMessage();
            /**
             * 打印堆栈信息
             */
            logger.error(String.format(msg, msg));
            return RspMsg.error(ResultStatus.SESSION_ERROR);
        } else {
            logger.error(e.getMessage(),e);
            return RspMsg.error(ResultStatus.SYSTEM_ERROR);
        }
    }
}
