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

package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.services.DownloadService;
import com.dryadandnaiad.sethlans.services.ServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * File created by Mario Estrella on 6/2/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
@Slf4j
@Profile({"SERVER", "DUAL"})
public class ServerBackgroundComponent {
    private final DownloadService downloadService;
    private final ServerService serverService;

    public ServerBackgroundComponent(DownloadService downloadService, ServerService serverService) {
        this.downloadService = downloadService;
        this.serverService = serverService;
    }

    @PostConstruct
    public void startBackgroundServices() throws InterruptedException {
        Thread.sleep(20000);
        downloadService.downloadBlenderFilesAsync();
        serverService.startBenchmarks();

    }
}
