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

package com.dryadandnaiad.sethlans.executor;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * File created by Mario Estrella on 5/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class MainExecutor {
    private static final MainExecutor INSTANCE = new MainExecutor();
    private final ThreadPoolTaskExecutor executor;

    public MainExecutor() {
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(35);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncExec-");
    }

    public static MainExecutor getInstance() {
        return INSTANCE;
    }

    public ThreadPoolTaskExecutor getExecutor() {
        return executor;
    }
}
