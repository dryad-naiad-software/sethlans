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

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.NodeDashBoardInfo;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.AccessKeyDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL", "SETUP"})
@RequestMapping("/api/info")
public class NodeInfoController {
    private static final Logger LOG = LoggerFactory.getLogger(NodeInfoController.class);
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private RenderTaskDatabaseService renderTaskDatabaseService;
    private AccessKeyDatabaseService accessKeyDatabaseService;
    private List<ComputeType> availableMethods = SethlansQueryUtils.getAvailableMethods();
    private Integer totalCores = new CPU().getCores();
    private List<GPUDevice> gpuDevices = GPU.listDevices();
    @Value("${sethlans.configDir}")
    private String configDir;

    @Value("${server.port}")
    private String sethlansPort;

    @Value("${sethlans.computeMethod}")
    private ComputeType computeType;

    @Value("${sethlans.cores}")
    private String cores;

    @Value("${sethlans.gpu_id}")
    private String deviceID;

    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @GetMapping(value = "/node_keep_alive")
    public boolean nodeKeepAlive(@RequestParam String connection_uuid) {
        if (firstTime) {
            return false;
        }
        LOG.debug("Keep alive received");
        return sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null;
    }

    @GetMapping(value = "/nodeinfo")
    public NodeInfo getNodeInfo(@RequestParam String access_key) {
        if (accessKeyDatabaseService.getByUUID(access_key) != null) {
            return SethlansNodeUtils.getNodeInfo();
        }
        return null;
    }

    @GetMapping(value = "/check_key")
    public boolean isKeyValid(@RequestParam String access_key) {
        if (firstTime) {
            return false;
        }
        if (accessKeyDatabaseService.getByUUID(access_key) != null) {
            LOG.debug("Key is present");
            return true;
        }
        LOG.debug("Key does not exist.");
        return false;
    }

    @GetMapping(value = {"/is_gpu_combined"})
    public Boolean isGpuCombined() {
        if (firstTime) {
            return false;
        }
        return Boolean.parseBoolean(getProperty(SethlansConfigKeys.COMBINE_GPU.toString()));
    }

    @GetMapping(value = {"/available_slots"})
    public Integer getAvailableSlots() {
        if (firstTime) {
            return 0;
        }
        int totalSlots = getTotalSlots();
        int slotsInUse = (int) renderTaskDatabaseService.tableSize();
        return totalSlots - slotsInUse;
    }

    @GetMapping(value = {"/node_total_slots"})
    public Integer getTotalSlots() {
        if (firstTime) {
            return 0;
        }
        boolean combined = Boolean.parseBoolean(getProperty(SethlansConfigKeys.COMBINE_GPU.toString()));
        NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
        switch (computeType) {
            case CPU:
                return 1;
            case GPU:
                if (combined) {
                    return 1;
                } else {
                    return nodeInfo.getSelectedGPUs().size();
                }
            case CPU_GPU:
                if (combined) {
                    return 2;
                } else {
                    return nodeInfo.getSelectedGPUs().size() + 1;
                }
        }
        return 0;
    }

    @GetMapping(value = {"/available_methods"})
    public List<ComputeType> getAvailableMethods() {
        return availableMethods;
    }

    @GetMapping(value = {"/total_cores"})
    public Integer getTotalCores() {
        return totalCores;
    }

    @GetMapping(value = {"/cpu_tile_size"})
    public String cpuTileSize() {
        return getProperty(SethlansConfigKeys.TILE_SIZE_CPU);
    }

    @GetMapping(value = {"/gpu_tile_size"})
    public String gpuTileSize() {
        return getProperty(SethlansConfigKeys.TILE_SIZE_GPU);
    }

    @GetMapping(value = {"/available_gpus"})
    public List<GPUDevice> getAvailableGPUs() {
        return gpuDevices;
    }

    @GetMapping(value = {"/client_available_gpu_models"})
    public List<String> getAvailableGPUModels() {
        List<String> models = new ArrayList<>();
        for (GPUDevice gpuDevice : gpuDevices) {
            models.add(gpuDevice.getModel());
        }
        return models;
    }

    @GetMapping(value = {"/client_selected_gpu_models"})
    public List<String> getSelectedGPUModels() {
        if (!getCurrentComputeType().equals(ComputeType.CPU)) {
            List<String> selectedGPUs = new ArrayList<>();
            try {
                String[] deviceList = getProperty(SethlansConfigKeys.GPU_DEVICE).split(",");
                List<GPUDevice> availableGPUs = GPU.listDevices();
                for (String deviceID : deviceList) {
                    for (GPUDevice gpu : availableGPUs) {
                        if (gpu.getDeviceID().equals(deviceID)) {
                            selectedGPUs.add(gpu.getModel());
                        }
                    }
                }
            } catch (NullPointerException e) {
                LOG.debug("No Selected GPU present");

            }
            return selectedGPUs;
        } else {
            return null;
        }
    }

    @GetMapping(value = {"/client_used_space"})
    public Long getClientUsedSpace() {
        return getClientTotalSpace() - getClientFreeSpace();
    }


    @GetMapping(value = {"/client_free_space"})
    public Long getClientFreeSpace() {
        return new File(getProperty(SethlansConfigKeys.CACHE_DIR)).getFreeSpace() / 1024 / 1024 / 1024;
    }

    @GetMapping(value = {"/client_total_space"})
    public Long getClientTotalSpace() {
        return new File(getProperty(SethlansConfigKeys.CACHE_DIR)).getTotalSpace() / 1024 / 1024 / 1024;

    }

    @GetMapping(value = {"/node_dashboard"})
    public NodeDashBoardInfo getNodeDashBoard() {
        if (firstTime) {
            return null;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        NodeDashBoardInfo nodeDashBoardInfo = new NodeDashBoardInfo();
        NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
        nodeDashBoardInfo.setComputeType(getCurrentComputeType());
        nodeDashBoardInfo.setCpuName(nodeInfo.getCpuinfo().getName());
        nodeDashBoardInfo.setFreeSpace(getClientFreeSpace());
        nodeDashBoardInfo.setSelectedCores(getSelectedCores());
        nodeDashBoardInfo.setTotalMemory(nodeInfo.getCpuinfo().getTotalMemory());
        nodeDashBoardInfo.setTotalSlots(getTotalSlots());
        nodeDashBoardInfo.setUsedSpace(getClientUsedSpace());
        nodeDashBoardInfo.setGpuCombined(isGpuCombined());
        nodeDashBoardInfo.setTotalSpace(getClientTotalSpace());
        nodeDashBoardInfo.setSelectedGPUModels(getSelectedGPUModels());
        nodeDashBoardInfo.setAvailableGPUModels(getAvailableGPUModels());
        return nodeDashBoardInfo;
    }

    @GetMapping(value = {"/selected_cores"})
    public String getSelectedCores() {
        return SethlansQueryUtils.getSelectedCores();
    }


    @GetMapping(value = {"/compute_type"})
    public ComputeType getCurrentComputeType() {
        return computeType;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setAccessKeyDatabaseService(AccessKeyDatabaseService accessKeyDatabaseService) {
        this.accessKeyDatabaseService = accessKeyDatabaseService;
    }

    @Autowired
    public void setRenderTaskDatabaseService(RenderTaskDatabaseService renderTaskDatabaseService) {
        this.renderTaskDatabaseService = renderTaskDatabaseService;
    }
}
