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

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private List<ComputeType> availableMethods = SethlansUtils.getAvailableMethods();
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

    @GetMapping(value = "/node_keep_alive")
    public boolean nodeKeepAlive() {
        return true;
    }

    @RequestMapping(value = "/nodeinfo", method = RequestMethod.GET)
    public NodeInfo getNodeInfo() {
        return SethlansUtils.getNodeInfo(new File(configDir + SethlansUtils.CONFIG_FILENAME));
    }

    @GetMapping(value = {"/is_gpu_combined"})
    public Boolean isGpuCombined() {
        return Boolean.parseBoolean(SethlansUtils.getProperty(SethlansConfigKeys.COMBINE_GPU.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME)));
    }

    @GetMapping(value = {"/node_total_slots"})
    public Integer getTotalSlots() {
        boolean combined = Boolean.parseBoolean(SethlansUtils.getProperty(SethlansConfigKeys.COMBINE_GPU.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME)));
        NodeInfo nodeInfo = getNodeInfo();
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
        return SethlansUtils.getProperty(SethlansConfigKeys.TILE_SIZE_CPU.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME));
    }

    @GetMapping(value = {"/gpu_tile_size"})
    public String gpuTileSize() {
        return SethlansUtils.getProperty(SethlansConfigKeys.TILE_SIZE_GPU.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME));
    }

    @GetMapping(value = {"/available_gpus"})
    public List<GPUDevice> getAvailableGPUs() {
        return gpuDevices;
    }

    @GetMapping(value = {"/client_selected_gpu_models"})
    public List<String> getSelectedGPUModels() {
        List<String> selectedGPUs = new ArrayList<>();
        try {
            List<String> deviceList = Arrays.asList(SethlansUtils.getProperty(SethlansConfigKeys.GPU_DEVICE.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME)).split(","));
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
    }

    @GetMapping(value = {"/client_used_space"})
    public Long getClientUsedSpace() {
        return getClientTotalSpace() - getClientFreeSpace();
    }


    @GetMapping(value = {"/client_free_space"})
    public Long getClientFreeSpace() {
        return new File(SethlansUtils.getProperty(SethlansConfigKeys.CACHE_DIR.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME))).getFreeSpace() / 1024 / 1024 / 1024;
    }

    @GetMapping(value = {"/client_total_space"})
    public Long getClientTotalSpace() {
        return new File(SethlansUtils.getProperty(SethlansConfigKeys.CACHE_DIR.toString(), new File(configDir + SethlansUtils.CONFIG_FILENAME))).getTotalSpace() / 1024 / 1024 / 1024;

    }


    @GetMapping(value = {"/selected_cores"})
    public String selectedCores() {
        return SethlansUtils.getSelectedCores(new File(configDir + SethlansUtils.CONFIG_FILENAME));
    }


    @GetMapping(value = {"/compute_type"})
    public ComputeType getCurrentComputeType() {
        return computeType;
    }
}
