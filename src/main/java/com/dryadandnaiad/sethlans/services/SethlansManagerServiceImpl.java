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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.SethlansApplication;
import com.dryadandnaiad.sethlans.executor.MainExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * File created by Mario Estrella on 5/31/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
public class SethlansManagerServiceImpl implements SethlansManagerService {
    private final ApplicationContext applicationContext;

    public SethlansManagerServiceImpl(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void restart() {
        SethlansApplication.restart();
    }

    @Override
    public void shutdown() {
        try {
            log.info("Shutting down Sethlans...");
            MainExecutor mainExecutor = MainExecutor.getInstance();
            mainExecutor.getExecutor().shutdown();
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.info("System Shutdown service closed");
            System.exit(0);
        }

    }
}
