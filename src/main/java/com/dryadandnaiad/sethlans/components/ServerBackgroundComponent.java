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
import com.dryadandnaiad.sethlans.services.blender.BlenderProcessRenderQueueService;
import com.dryadandnaiad.sethlans.services.blender.BlenderQueueService;
import com.dryadandnaiad.sethlans.services.blender.NodeSlotUpdateService;
import com.dryadandnaiad.sethlans.services.network.NodeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private BlenderProcessRenderQueueService blenderProcessRenderQueueService;
    private BlenderDownloadService blenderDownloadService;
    private NodeQueryService nodeQueryService;
    private NodeSlotUpdateService nodeSlotUpdateService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerBackgroundComponent.class);

    @PostConstruct
    public void projectQueue() {
        LOG.debug("Starting Project Queue Service");
        blenderQueueService.startQueue();
        blenderQueueService.queueUpdateList();
    }

    @PostConstruct
    public void renderQueue() {
        LOG.debug("Starting Render Queue Service");
        blenderProcessRenderQueueService.startRenderProcessingQueue();
        nodeSlotUpdateService.startRenderNodeUpdateQueue();

    }


    @PostConstruct
    public void startBlenderDownload() {
        blenderDownloadService.downloadRequestedBlenderFilesAsync();

    }

    @PostConstruct
    public void startNodeQueryService() {
        LOG.debug("Starting Node Query Service");
        nodeQueryService.start();
    }

    @Autowired
    public void setBlenderQueueService(BlenderQueueService blenderQueueService) {
        this.blenderQueueService = blenderQueueService;
    }


    @Autowired
    public void setBlenderDownloadService(BlenderDownloadService blenderDownloadService) {
        this.blenderDownloadService = blenderDownloadService;
    }

    @Autowired
    public void setBlenderProcessRenderQueueService(BlenderProcessRenderQueueService blenderProcessRenderQueueService) {
        this.blenderProcessRenderQueueService = blenderProcessRenderQueueService;
    }

    @Autowired
    public void setNodeQueryService(NodeQueryService nodeQueryService) {
        this.nodeQueryService = nodeQueryService;
    }

    @Autowired
    public void setNodeSlotUpdateService(NodeSlotUpdateService nodeSlotUpdateService) {
        this.nodeSlotUpdateService = nodeSlotUpdateService;
    }
}
