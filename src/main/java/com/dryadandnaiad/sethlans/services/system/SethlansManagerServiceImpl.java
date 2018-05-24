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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.restart.RestartEndpoint;
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
    private RestartEndpoint restartEndpoint;

    @Override
    @Async
    public void shutdown() {
        try {
            Thread.sleep(5000);
            System.exit(0);
        } catch (InterruptedException e) {
            LOG.info("System Shutdown service closed");

        }

    }

    @Override
    @Async
    public void restart() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOG.info("System Restart service closed");
        }
        Thread thread = new Thread(() -> restartEndpoint.invoke());
        thread.setDaemon(false);
        thread.start();
    }

    @Autowired
    public void setRestartEndpoint(RestartEndpoint restartEndpoint) {
        this.restartEndpoint = restartEndpoint;
    }
}
