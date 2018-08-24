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

import com.dryadandnaiad.sethlans.domains.database.server.AccessKey;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.AccessKeyDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 7/25/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/management")
@Profile({"NODE", "DUAL"})


public class AdminNodeController {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private AccessKeyDatabaseService accessKeyDatabaseService;

    @Value("${sethlans.computeMethod}")
    private ComputeType selectedComputeMethod;

    @Value("${sethlans.gpu_id}")
    private String gpuIds;

    @Value("${sethlans.tileSizeGPU}")
    private String tileSizeGPU;

    @Value("${sethlans.tileSizeCPU}")
    private String titleSizeCPU;

    @Value("${sethlans.cores}")
    private String selectedCores;

    @GetMapping(value = {"/current_tilesize_gpu"})
    public Integer getCurrentTileSizeGPU() {
        return Integer.parseInt(this.tileSizeGPU);
    }

    @GetMapping(value = {"/current_cores"})
    public Integer getCurrentCores() {
        if (this.selectedCores == null || this.selectedCores.isEmpty()) {
            return new CPU().getCores();
        } else {
            return Integer.parseInt(this.selectedCores);
        }
    }

    @GetMapping(value = {"/current_node"})
    public SetupNode getCurrentNode() {
        SetupNode setupNode = new SetupNode();
        if (!getSelectedComputeMethod().equals(ComputeType.CPU)) {
            List<String> gpuIdsList = Arrays.asList(gpuIds.split(","));
            setupNode.setSelectedGPUDeviceIDs(gpuIdsList);
            setupNode.setGpuEmpty(false);
            setupNode.setCombined(Boolean.parseBoolean(getProperty(SethlansConfigKeys.COMBINE_GPU)));
        }
        setupNode.setComputeMethod(getSelectedComputeMethod());
        setupNode.setCores(getCurrentCores());
        setupNode.setTileSizeCPU(getCurrentTileSizeCPU());
        setupNode.setTileSizeGPU(getCurrentTileSizeGPU());
        return setupNode;
    }

    @GetMapping(value = "/get_access_key_list")
    public List<AccessKey> getAccessKeyList() {
        return accessKeyDatabaseService.listAll();
    }

    @GetMapping(value = {"/selected_gpus"})
    public List<GPUDevice> getSelectedGPU() {
        String[] gpuIdsList = gpuIds.split(",");
        List<GPUDevice> gpuDeviceList = GPU.listDevices();
        List<GPUDevice> selectedGPUs = new ArrayList<>();
        for (String gpuID : gpuIdsList) {
            for (GPUDevice aGpuDeviceList : gpuDeviceList) {
                if (aGpuDeviceList.getDeviceID().equals(gpuID)) {
                    selectedGPUs.add(aGpuDeviceList);
                }
            }
        }
        return selectedGPUs;
    }

    @GetMapping(value = {"/server_list"})
    public List<SethlansServer> getServers() {
        return sethlansServerDatabaseService.listAll();
    }

    @GetMapping(value = {"/server_list_size"})
    public Integer getServerListSize() {
        return sethlansServerDatabaseService.listAll().size();
    }

    @GetMapping(value = {"/current_tilesize_cpu"})
    public Integer getCurrentTileSizeCPU() {
        return Integer.parseInt(this.titleSizeCPU);
    }

    @GetMapping(value = {"/selected_compute_method"})
    public ComputeType getSelectedComputeMethod() {
        return this.selectedComputeMethod;
    }


    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setAccessKeyDatabaseService(AccessKeyDatabaseService accessKeyDatabaseService) {
        this.accessKeyDatabaseService = accessKeyDatabaseService;
    }
}
