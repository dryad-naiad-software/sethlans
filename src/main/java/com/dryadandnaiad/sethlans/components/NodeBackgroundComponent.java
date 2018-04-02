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

import com.dryadandnaiad.sethlans.services.blender.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.blender.BlenderRenderService;
import com.dryadandnaiad.sethlans.services.network.MulticastSenderService;
import com.dryadandnaiad.sethlans.services.network.NodeSendUpdateService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
@Profile({"NODE", "DUAL"})
public class NodeBackgroundComponent {

    private static final Logger LOG = LoggerFactory.getLogger(NodeBackgroundComponent.class);
    private MulticastSenderService multicastSenderService;
    private NodeSendUpdateService nodeSendUpdateService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private BlenderRenderService blenderRenderService;

    @Value("${server.port}")
    private String sethlansPort;


    @PostConstruct
    public void startNodeMulticast() {
        String ip = SethlansUtils.getIP();
        LOG.debug("Sethlans Host IP: " + ip);
        multicastSenderService.sendSethlansIPAndPort(ip, sethlansPort);
    }

    @PostConstruct
    public void startNodeStatusUpdates() {
        nodeSendUpdateService.sendUpdateOnStart();
    }

    @PostConstruct
    public void startBenchmarks() {
        blenderBenchmarkService.benchmarkOnNodeRestart();

    }

    @PostConstruct
    public void startRender() {
        blenderRenderService.clearQueueOnNodeRestart();

    }

    @Autowired
    public void setMulticastSenderService(MulticastSenderService multicastSenderService) {
        this.multicastSenderService = multicastSenderService;
    }

    @Autowired
    public void setNodeSendUpdateService(NodeSendUpdateService nodeSendUpdateService) {
        this.nodeSendUpdateService = nodeSendUpdateService;
    }


    @Autowired
    public void setBlenderBenchmarkService(BlenderBenchmarkService blenderBenchmarkService) {
        this.blenderBenchmarkService = blenderBenchmarkService;
    }

    @Autowired
    public void setBlenderRenderService(BlenderRenderService blenderRenderService) {
        this.blenderRenderService = blenderRenderService;
    }
}
