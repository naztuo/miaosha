package com.naztuo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public Object defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        LOGGER.error(e.getMessage(),e);
        return e.getMessage();
    }

}
