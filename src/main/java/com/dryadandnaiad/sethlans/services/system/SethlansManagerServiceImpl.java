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

package com.dryadandnaiad.sethlans.services.system;

import com.dryadandnaiad.sethlans.executor.SethlansExecutor;
import com.dryadandnaiad.sethlans.utils.SethlansState;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 3/22/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansManagerServiceImpl implements SethlansManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansManagerServiceImpl.class);
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Async
    public void shutdown() {
        try {
            Thread.sleep(2000);
            System.exit(0);
        } catch (InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

    }

    @Override
    @Async
    public void restart() {
        try {
            SethlansState sethlansState = SethlansState.getInstance();
            Thread.sleep(2000);
            sethlansState.sethlansActive = false;
            SethlansExecutor sethlansExecutor = SethlansExecutor.getInstance();
            sethlansExecutor.getExecutor().shutdown();
            SpringApplication.exit(applicationContext, () -> 0);
        } catch (InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

    }


}
