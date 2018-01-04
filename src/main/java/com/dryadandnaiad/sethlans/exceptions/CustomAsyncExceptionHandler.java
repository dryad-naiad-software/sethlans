package com.dryadandnaiad.sethlans.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

/**
 * Created Mario Estrella on 1/3/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
public class CustomAsyncExceptionHandler
        implements AsyncUncaughtExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

    @Override
    public void handleUncaughtException(
            Throwable throwable, Method method, Object... obj) {

        LOG.error("Exception message - " + throwable.getMessage());
        LOG.error("Method name - " + method.getName());
        for (Object param : obj) {
            LOG.error("Parameter value - " + param);
        }
    }

}