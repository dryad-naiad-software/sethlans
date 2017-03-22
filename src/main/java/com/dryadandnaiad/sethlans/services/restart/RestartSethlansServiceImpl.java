/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.restart;

import com.dryadandnaiad.sethlans.components.SethlansSystrayComponent;
import com.dryadandnaiad.sethlans.services.interfaces.RestartSethlansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 3/22/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class RestartSethlansServiceImpl implements RestartSethlansService {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private RestartEndpoint restartEndpoint;

    @Override
    public void restart() {
        Thread restartThread = new Thread(() -> restartEndpoint.restart());
        restartThread.setDaemon(false);
        restartThread.start();
        SethlansSystrayComponent.teardown();
    }
}
