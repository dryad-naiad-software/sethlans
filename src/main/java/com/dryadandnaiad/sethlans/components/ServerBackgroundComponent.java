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

package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.services.blender.BlenderDownloadService;
import com.dryadandnaiad.sethlans.services.blender.BlenderQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created Mario Estrella on 12/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Profile({"SERVER", "DUAL"})
@Component
public class ServerBackgroundComponent {
    private BlenderQueueService blenderQueueService;

    private BlenderDownloadService blenderDownloadService;

    @PostConstruct
    public void startRenderQueue() {
        blenderQueueService.startQueue();
    }


    @PostConstruct
    public void startBlenderDownload() {
        blenderDownloadService.downloadRequestedBlenderFilesAsync();

    }

    @Autowired
    public void setBlenderQueueService(BlenderQueueService blenderQueueService) {
        this.blenderQueueService = blenderQueueService;
    }


    @Autowired
    public void setBlenderDownloadService(BlenderDownloadService blenderDownloadService) {
        this.blenderDownloadService = blenderDownloadService;
    }
}
