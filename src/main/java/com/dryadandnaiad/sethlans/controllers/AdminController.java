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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.Log;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettingsInfo;
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.system.SethlansLogRetrievalService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created Mario Estrella on 3/2/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/management")
public class AdminController {
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private SethlansLogRetrievalService sethlansLogRetrievalService;
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansManagerService sethlansManagerService;

    @Value("${sethlans.gpu_id}")
    private String gpuIds;

    @Value("${sethlans.cores}")
    private String selectedCores;

    @Value("${sethlans.computeMethod}")
    private ComputeType selectedComputeMethod;

    @Value("${sethlans.tileSizeGPU}")
    private String tileSizeGPU;

    @Value("${sethlans.tileSizeCPU}")
    private String titleSizeCPU;

    @GetMapping(value = "/restart")
    public void restart() {
        sethlansManagerService.restart();
        LOG.debug("Restarting Sethlans...");
    }

    @GetMapping(value = "/shutdown")
    public void shutdown() {
        sethlansManagerService.shutdown();
        LOG.debug("Shutting down Sethlans...");
    }

    @GetMapping(value = "/user_list")
    public List<UserInfo> sethlansUserList() {
        List<SethlansUser> sethlansUsers = sethlansUserDatabaseService.listAll();
        List<UserInfo> userInfoList = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            UserInfo userToSend = new UserInfo();
            userToSend.setUsername(sethlansUser.getUsername());
            userToSend.setActive(sethlansUser.isActive());
            userToSend.setRoles(sethlansUser.getRoles());
            userToSend.setEmail(sethlansUser.getEmail());
            userToSend.setId(sethlansUser.getId());
            userToSend.setLastUpdated(sethlansUser.getLastUpdated());
            userToSend.setDateCreated(sethlansUser.getDateCreated());
            userInfoList.add(userToSend);
        }
        return userInfoList;
    }

    @GetMapping(value = {"/node_check"})
    public SethlansNode checkNode(@RequestParam String ip, @RequestParam String port) {
        return nodeDiscoveryService.discoverUnicastNode(ip, port);
    }

    @GetMapping(value = {"/get_logs"})
    public List<Log> getSethlansLogs() {
        return sethlansLogRetrievalService.sethlansLogList();
    }

    @GetMapping(value = {"node_scan"})
    public List<SethlansNode> nodeScan() throws InterruptedException {
        nodeDiscoveryService.resetNodeList();
        nodeDiscoveryService.multicastDiscovery();
        Thread.sleep(20000);
        return nodeDiscoveryService.discoverMulticastNodes();
    }

    @GetMapping(value = {"/node_update_info/{id}"})
    public SethlansNode getNodeById(@PathVariable Long id) {
        return sethlansNodeDatabaseService.getById(id);
    }

    @GetMapping(value = {"/node_list"})
    public List<SethlansNode> getNodes() {
        return sethlansNodeDatabaseService.listAll();
    }

    @GetMapping(value = {"/nodes_updating_list"})
    public List<SethlansNode> getNodesUpdating() {
        List<SethlansNode> listToSend = new ArrayList<>();
        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
            if (!sethlansNode.isActive() || !sethlansNode.isBenchmarkComplete()) {
                listToSend.add(sethlansNode);
            }
        }
        return listToSend;
    }

    @GetMapping(value = {"/node_list_size"})
    public Integer getNodeListSize() {
        return sethlansNodeDatabaseService.listAll().size();
    }

    @GetMapping(value = {"/server_list"})
    public List<SethlansServer> getServers() {
        return sethlansServerDatabaseService.listAll();
    }

    @GetMapping(value = {"/server_list_size"})
    public Integer getServerListSize() {
        return sethlansServerDatabaseService.listAll().size();
    }


    @GetMapping(value = "/primary_blender_version")
    public Map primaryBlenderVersion() {
        return Collections.singletonMap("primary_blender",
                SethlansUtils.getProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION.toString()));
    }

    @GetMapping(value = "/blender_versions")
    public Set<String> blenderBinaryList() {
        List<BlenderBinary> blenderBinaries = blenderBinaryDatabaseService.listAll();
        Set<String> listOfVersions = new HashSet<>();
        for (BlenderBinary blenderBinary : blenderBinaries) {
            listOfVersions.add(blenderBinary.getBlenderVersion());
        }
        return listOfVersions;
    }

    @GetMapping(value = "/get_current_binary_os/{version}")
    public List<BlenderBinaryOS> blenderBinaryOSList(@PathVariable String version) {
        LOG.debug(version);
        List<BlenderBinaryOS> blenderBinaryOS = new ArrayList<>();
        List<BlenderBinary> blenderBinaries = blenderBinaryDatabaseService.listAll();
        for (BlenderBinary blenderBinary : blenderBinaries) {
            if (blenderBinary.getBlenderVersion().equals(version)) {
                blenderBinaryOS.add(BlenderBinaryOS.valueOf(blenderBinary.getBlenderBinaryOS()));
            }
        }
        return blenderBinaryOS;
    }

    @GetMapping(value = "/current_settings")
    public SethlansSettingsInfo sethlansSettingsInfo() {
        return SethlansUtils.getSettings();
    }

    @GetMapping(value = {"/selected_gpus"})
    public List<GPUDevice> getSelectedGPU() {
        List<String> gpuIdsList = Arrays.asList(gpuIds.split(","));
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

    @GetMapping(value = {"/current_tilesize_cpu"})
    public Integer getCurrentTileSizeCPU() {
        return Integer.parseInt(this.titleSizeCPU);
    }

    @GetMapping(value = {"/selected_compute_method"})
    public ComputeType getSelectedComputeMethod() {
        return this.selectedComputeMethod;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

    @Autowired
    public void setSethlansLogRetrievalService(SethlansLogRetrievalService sethlansLogRetrievalService) {
        this.sethlansLogRetrievalService = sethlansLogRetrievalService;
    }
}
