/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.blender.benchmark.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

/**
 * Created Mario Estrella on 12/25/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ServerBackgroundController {
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private RenderQueueDatabaseService renderQueueDatabaseService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private NodeDiscoveryService nodeDiscoveryService;
    private QueueService queueService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerBackgroundController.class);


    @PostMapping(value = "/api/update/node_idle_notification")
    public void nodeIdleNotification(@RequestParam String connection_uuid, ComputeType compute_type) {
        if (renderQueueDatabaseService.tableSize() > 0) {
            queueService.queueIdleNode(connection_uuid, compute_type);
        }
    }


    @RequestMapping(value = "/api/update/node_status_update", method = RequestMethod.GET)
    public void nodeStatusToServerUpdate(@RequestParam String connection_uuid) {
        // This is a pull request.  UUID is provided and the server will update it's information from the node.
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            SethlansNode sethlansNodetoUpdate = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
            LOG.debug("Sync request received for " + sethlansNodetoUpdate.getHostname());
            SethlansNode tempNode = nodeDiscoveryService.discoverUnicastNode(sethlansNodetoUpdate.getIpAddress(), sethlansNodetoUpdate.getNetworkPort());
            if (sethlansNodetoUpdate.isActive()) {
                sethlansNodetoUpdate.setBenchmarkComplete(false);
                switch (tempNode.getComputeType()) {
                    case CPU_GPU:
                        if (tempNode.isCombined()) {
                            sethlansNodetoUpdate.setTotalRenderingSlots(2);
                            sethlansNodetoUpdate.setAvailableRenderingSlots(2);
                        } else {
                            sethlansNodetoUpdate.setTotalRenderingSlots(tempNode.getSelectedGPUs().size() + 1);
                            sethlansNodetoUpdate.setAvailableRenderingSlots(tempNode.getSelectedGPUs().size() + 1);
                        }
                        break;
                    case GPU:
                        if (tempNode.isCombined()) {
                            sethlansNodetoUpdate.setTotalRenderingSlots(1);
                            sethlansNodetoUpdate.setAvailableRenderingSlots(1);
                        } else {
                            sethlansNodetoUpdate.setTotalRenderingSlots(tempNode.getSelectedGPUs().size());
                            sethlansNodetoUpdate.setAvailableRenderingSlots(tempNode.getSelectedGPUs().size());
                        }
                        break;
                    case CPU:
                        sethlansNodetoUpdate.setTotalRenderingSlots(1);
                        sethlansNodetoUpdate.setAvailableRenderingSlots(1);
                        break;

                }

                updateNode(sethlansNodetoUpdate, tempNode);
                try {
                    Thread.sleep(10000);
                    blenderBenchmarkService.sendBenchmarktoNode(sethlansNodetoUpdate);
                } catch (InterruptedException e) {
                    LOG.debug(Throwables.getStackTraceAsString(e));
                }
            } else {
                updateNode(sethlansNodetoUpdate, tempNode);
            }
            LOG.debug(sethlansNodetoUpdate.getHostname() + " has been synced.");
        }
    }

    private void updateNode(SethlansNode sethlansNodetoUpdate, SethlansNode tempNode) {
        sethlansNodetoUpdate.setCombined(tempNode.isCombined());
        sethlansNodetoUpdate.setComputeType(tempNode.getComputeType());
        sethlansNodetoUpdate.setCpuinfo(tempNode.getCpuinfo());
        sethlansNodetoUpdate.setSelectedCores(tempNode.getSelectedCores());
        sethlansNodetoUpdate.setSelectedDeviceID(tempNode.getSelectedDeviceID());
        sethlansNodetoUpdate.setSelectedGPUs(tempNode.getSelectedGPUs());
        sethlansNodetoUpdate.setSelectedGPUModels(tempNode.getSelectedGPUModels());
        LOG.debug("Saving changes to database.");
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNodetoUpdate);
    }


    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setBlenderBenchmarkService(BlenderBenchmarkService blenderBenchmarkService) {
        this.blenderBenchmarkService = blenderBenchmarkService;
    }

    @Autowired
    public void setRenderQueueDatabaseService(RenderQueueDatabaseService renderQueueDatabaseService) {
        this.renderQueueDatabaseService = renderQueueDatabaseService;
    }

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}
