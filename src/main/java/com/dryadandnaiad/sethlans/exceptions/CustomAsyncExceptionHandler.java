/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.exceptions;

import com.google.common.base.Throwables;
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
        if (method.getName().equals("startRenderProcessingQueue")) {

        }
        LOG.error("Stacktrace: " + Throwables.getStackTraceAsString(throwable));
    }

}