/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.info.ServerDashBoardInfo;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */

@RestController
@Profile({"SERVER", "DUAL", "SETUP"})
@RequestMapping("/api/info")
public class ServerInfoController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerInfoController.class);
    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;

    @Value("${sethlans.configDir}")
    private String configDir;

    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private GetRawDataService getRawDataService;


    @GetMapping(value = {"/total_nodes"})
    public long getTotalNodes() {
        return sethlansNodeDatabaseService.tableSize();
    }

    @GetMapping(value = {"/inactive_nodes"})
    public int getInactiveNodes() {
        return sethlansNodeDatabaseService.inactiveNodeList().size();
    }

    @GetMapping(value = {"/disabled_nodes"})
    public int getDisabledNodes() {
        return sethlansNodeDatabaseService.disabledNodeList().size();
    }

    @GetMapping(value = {"/active_nodes"})
    public int getActiveNodes() {
        return sethlansNodeDatabaseService.activeNodeList().size();
    }

    @GetMapping(value = {"/active_nodes_cpu"})
    public int getActiveCPUNodes() {
        return sethlansNodeDatabaseService.activeCPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_gpu"})
    public int getActiveGPUNodes() {
        return sethlansNodeDatabaseService.activeGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_cpu_gpu"})
    public int getActiveCPUGPUNodes() {
        return sethlansNodeDatabaseService.activeCPUGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_value_array"})
    public List<Integer> getNumberOfActiveNodesArray() {
        List<Integer> numberOfActiveNodesArray = new ArrayList<>();
        numberOfActiveNodesArray.add(getActiveCPUNodes());
        numberOfActiveNodesArray.add(getActiveGPUNodes());
        numberOfActiveNodesArray.add(getActiveCPUGPUNodes());
        return numberOfActiveNodesArray;
    }

    @GetMapping(value = {"/installed_blender_versions"})
    public List<String> getInstalledBlenderVersions() {
        return blenderBinaryDatabaseService.installedBlenderVersions();
    }

    @GetMapping(value = {"/server_rendering_slots"})
    public int getRenderingSlots() {
        if (sethlansNodeDatabaseService.activeNodes()) {
            return getTotalSlots() - getIdleSlots();
        }
        return 0;

    }

    @GetMapping(value = {"/server_idle_slots"})
    public int getIdleSlots() {
        int availableSlotsCount = 0;
        if (sethlansNodeDatabaseService.activeNodes()) {
            Gson gson = new Gson();
            for (SethlansNode sethlansNode : sethlansNodeDatabaseService.activeNodeList()) {
                List<String> deviceIdsInUse =
                        gson.fromJson(getRawDataService.getNodeResult("https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() +
                                "/api/info/used_device_ids"), new TypeToken<List<String>>() {
                        }.getType());
                int slotsToAdd = SethlansNodeUtils.getAvailableDeviceIds(sethlansNode, deviceIdsInUse).size();
                availableSlotsCount = availableSlotsCount + slotsToAdd;
            }
            LOG.debug("Available Slot Count " + availableSlotsCount);
        }
        return availableSlotsCount;

    }

    @GetMapping(value = {"/server_total_slots"})
    public int getTotalSlots() {
        int totalSlotsCount = 0;
        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
            if (sethlansNode.isActive() && !sethlansNode.isDisabled() && sethlansNode.isBenchmarkComplete()) {
                totalSlotsCount = totalSlotsCount + sethlansNode.getTotalRenderingSlots();
            }
        }
        return totalSlotsCount;
    }

    @GetMapping(value = {"/server_used_space"})
    public Long getClientUsedSpace() {
        return getClientTotalSpace() - getClientFreeSpace();
    }


    @GetMapping(value = {"/server_free_space"})
    public Long getClientFreeSpace() {
        return new File(getProperty(SethlansConfigKeys.PROJECT_DIR)).getFreeSpace() / 1024 / 1024 / 1024;
    }

    @GetMapping(value = {"/server_total_space"})
    public Long getClientTotalSpace() {
        return new File(getProperty(SethlansConfigKeys.PROJECT_DIR)).getTotalSpace() / 1024 / 1024 / 1024;

    }

    @GetMapping(value = {"/server_dashboard"})
    public ServerDashBoardInfo getDashBoard() {
        if (firstTime) {
            return null;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        ServerDashBoardInfo serverDashBoardInfo = new ServerDashBoardInfo();
        CPU cpu = new CPU();
        serverDashBoardInfo.setActiveNodes(getActiveNodes());
        serverDashBoardInfo.setCpuName(cpu.getName());
        serverDashBoardInfo.setDisabledNodes(getDisabledNodes());
        serverDashBoardInfo.setFreeSpace(getClientFreeSpace());
        serverDashBoardInfo.setInactiveNodes(getInactiveNodes());
        serverDashBoardInfo.setNumberOfActiveNodesArray(getNumberOfActiveNodesArray());
        serverDashBoardInfo.setTotalMemory(cpu.getTotalMemory());
        serverDashBoardInfo.setTotalNodes(getTotalNodes());
        serverDashBoardInfo.setTotalSlots(getTotalSlots());
        serverDashBoardInfo.setUsedSpace(getClientUsedSpace());
        serverDashBoardInfo.setTotalSpace(getClientTotalSpace());
        serverDashBoardInfo.setIdleSlots(getIdleSlots());
        serverDashBoardInfo.setRenderingSlots(getRenderingSlots());
        return serverDashBoardInfo;
    }


    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setGetRawDataService(GetRawDataService getRawDataService) {
        this.getRawDataService = getRawDataService;
    }
}
