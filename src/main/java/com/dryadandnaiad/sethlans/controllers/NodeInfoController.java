/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
public class NodeInfoController {
    private static final Logger LOG = LoggerFactory.getLogger(NodeInfoController.class);

    @Value("${server.port}")
    private String sethlansPort;

    @Value("${sethlans.computeMethod}")
    private ComputeType computeType;

    @Value("${sethlans.cores}")
    private String cores;

    @Value("${sethlans.gpu_id}")
    private String deviceID;


    @RequestMapping(value = "/api/info/nodeinfo", method = RequestMethod.GET)
    public NodeInfo nodeInfo() {
        LOG.debug("Node info requested.");
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.populateNodeInfo();

        nodeInfo.setNetworkPort(sethlansPort);
        nodeInfo.setComputeType(computeType);

        if (computeType == ComputeType.CPU_GPU || computeType == ComputeType.CPU) {
            nodeInfo.setCpuinfo();
            nodeInfo.setSelectedCores(cores);
        }

        if (computeType == ComputeType.GPU || computeType == ComputeType.CPU_GPU) {
            List<String> deviceList = Arrays.asList(deviceID.split(","));
            nodeInfo.setCpuinfo();
            nodeInfo.setSelectedDeviceID(deviceList);
            nodeInfo.setSelectedGPUs();

        }
        return nodeInfo;
    }
}