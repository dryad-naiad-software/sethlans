/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.exceptions;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.BeanCreationException;

import java.lang.reflect.Method;

/**
 * File created by Mario Estrella on 4/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(
            Throwable throwable, Method method, Object... obj) {
        if (throwable instanceof BeanCreationException) {
            log.error("Bean creation failure. Likely during shutdown and DB is being accessed after it was closed. This can be ignored.");
        } else {
            log.error("Exception message - " + throwable.getMessage());

            log.error("Method name - " + method.getName());
            for (Object param : obj) {
                log.error("Parameter value - " + param);
            }

            log.error("Stacktrace: " + Throwables.getStackTraceAsString(throwable));
        }


    }
}
