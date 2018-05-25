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

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.executor.SethlansExecutor;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Created Mario Estrella on 3/22/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansManagerServiceImpl implements SethlansManagerService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansManagerServiceImpl.class);
    private RestartEndpoint restartEndpoint;
    private ConfigurableEnvironment env;


    @Async
    @Override
    public void shutdown() {
        try {
            Thread.sleep(5000);
            SethlansExecutor sethlansExecutor = SethlansExecutor.getInstance();
            sethlansExecutor.getExecutor().shutdown();
            System.exit(0);
        } catch (InterruptedException e) {
            LOG.info("System Shutdown service closed");
        }

    }

    @Override
    public void restart() {
        SethlansExecutor sethlansExecutor = SethlansExecutor.getInstance();
        sethlansExecutor.getExecutor().shutdown();
        Thread thread = new Thread(() -> {
            if (SethlansUtils.getProperty(SethlansConfigKeys.MODE.toString()) != null) {
                if (Arrays.asList(env.getActiveProfiles()).contains("SETUP")) {
                    env.setActiveProfiles(SethlansUtils.getProperty(SethlansConfigKeys.MODE.toString()));
                }
            }
            restartEndpoint.restart();
        });
        thread.setDaemon(false);
        thread.start();
    }

    @Autowired
    public void setRestartEndpoint(RestartEndpoint restartEndpoint) {
        this.restartEndpoint = restartEndpoint;
    }

    @Autowired
    public void setEnv(ConfigurableEnvironment env) {
        this.env = env;
    }
}
