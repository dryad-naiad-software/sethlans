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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.blender.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private BlenderBenchmarkService blenderBenchmarkService;
    private NodeDiscoveryService nodeDiscoveryService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerBackgroundController.class);

    @RequestMapping(value = "/api/update/node_status_update", method = RequestMethod.GET)
    public void nodeStatusToServerUpdate(@RequestParam String connection_uuid) {
        // This is a pull request.  UUID is provided and the server will update it's information from the node.
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            SethlansNode sethlansNodetoUpdate = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
            LOG.debug("Sync request received for " + sethlansNodetoUpdate.getHostname());
            SethlansNode tempNode = nodeDiscoveryService.discoverUnicastNode(sethlansNodetoUpdate.getIpAddress(), sethlansNodetoUpdate.getNetworkPort());
            boolean checkNode = checkNodeUpdate(sethlansNodetoUpdate, tempNode);
            if (checkNode && sethlansNodetoUpdate.isActive()) {
                sethlansNodetoUpdate.setBenchmarkComplete(false);
                updateNode(sethlansNodetoUpdate, tempNode);
                try {
                    Thread.sleep(10000);
                    blenderBenchmarkService.sendBenchmarktoNode(sethlansNodetoUpdate);
                } catch (InterruptedException e) {
                    LOG.debug(Throwables.getStackTraceAsString(e));
                }

            } else if (checkNode) {
                updateNode(sethlansNodetoUpdate, tempNode);
            }
            LOG.debug(sethlansNodetoUpdate.getHostname() + " has been synced.");


        }
    }

    private void updateNode(SethlansNode sethlansNodetoUpdate, SethlansNode tempNode) {
        sethlansNodetoUpdate.setComputeType(tempNode.getComputeType());
        sethlansNodetoUpdate.setCpuinfo(tempNode.getCpuinfo());
        sethlansNodetoUpdate.setSelectedCores(tempNode.getSelectedCores());
        sethlansNodetoUpdate.setSelectedDeviceID(tempNode.getSelectedDeviceID());
        sethlansNodetoUpdate.setSelectedGPUs(tempNode.getSelectedGPUs());
        LOG.debug("Saving changes to database.");
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNodetoUpdate);
    }

    private boolean checkNodeUpdate(SethlansNode originalNode, SethlansNode nodeUpdate) {
        if (!originalNode.getComputeType().equals(nodeUpdate.getComputeType())) {
            LOG.debug("Compute Type Changed");
            return true;
        }
        if (nodeUpdate.getComputeType().equals(ComputeType.CPU) || nodeUpdate.getComputeType().equals(ComputeType.CPU_GPU)) {
            if (!originalNode.getSelectedCores().equals(nodeUpdate.getSelectedCores())) {
                LOG.debug("Number of Cores Changed.  Now: " + nodeUpdate.getSelectedCores());
                return true;
            }
        }

        if (nodeUpdate.getComputeType().equals(ComputeType.GPU) || nodeUpdate.getComputeType().equals(ComputeType.CPU_GPU)) {
            if (!originalNode.getSelectedDeviceID().toString().equals(nodeUpdate.getSelectedDeviceID().toString())) {
                LOG.debug("Selected CUDA Changed. Now: " + nodeUpdate.getSelectedDeviceID());
                return true;
            }
            if (!originalNode.getSelectedGPUs().toString().equals(nodeUpdate.getSelectedGPUs().toString())) {
                LOG.debug("Selected GPUs Changed. Now: " + nodeUpdate.getSelectedGPUs().toString());
                return true;
            }
        }

        return false;
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
}
