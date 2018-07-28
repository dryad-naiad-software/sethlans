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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.info.BlenderBinaryInfo;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

/**
 * Created Mario Estrella on 7/25/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/management")
@Profile({"SERVER", "DUAL"})
public class AdminServerController {
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;


    @GetMapping(value = "/get_current_binary_os/{version}")
    public List<BlenderBinaryOS> blenderBinaryOSList(@PathVariable String version) {
        List<BlenderBinaryOS> blenderBinaryOS = new ArrayList<>();
        List<BlenderBinary> blenderBinaries = blenderBinaryDatabaseService.listAll();
        for (BlenderBinary blenderBinary : blenderBinaries) {
            if (blenderBinary.getBlenderVersion().equals(version)) {
                blenderBinaryOS.add(BlenderBinaryOS.valueOf(blenderBinary.getBlenderBinaryOS()));
            }
        }
        return blenderBinaryOS;
    }

    @GetMapping(value = "/installed_blender_versions")
    public Map blenderBinaryList() {
        Set<String> listOfVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        return Collections.singletonMap("installedBlenderVersions", listOfVersions);
    }

    @GetMapping(value = "/get_blender_list")
    public List<BlenderBinaryInfo> getBlenderBinaryInfoList() {
        List<BlenderBinaryInfo> blenderBinaryInfoList = new ArrayList<>();
        Set<String> listOfVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        for (String version : listOfVersions) {
            BlenderBinaryInfo blenderBinaryInfo = new BlenderBinaryInfo();
            blenderBinaryInfo.setVersion(version);
            blenderBinaryInfo.setBinaryOSList(blenderBinaryOSList(version));
            if (SethlansUtils.getProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION.toString()).equals(version)) {
                blenderBinaryInfo.setActive(true);
            }
            blenderBinaryInfoList.add(blenderBinaryInfo);
        }
        return blenderBinaryInfoList;
    }


    @GetMapping(value = "/get_key_from_server")
    public String getAccessKeyFromServer() {
        return SethlansUtils.getProperty(SethlansConfigKeys.ACCESS_KEY.toString());
    }

    @GetMapping(value = {"node_scan"})
    public List<SethlansNode> nodeScan() throws InterruptedException {
        nodeDiscoveryService.resetNodeList();
        nodeDiscoveryService.multicastDiscovery();
        Thread.sleep(20000);
        return nodeDiscoveryService.discoverMulticastNodes();
    }

    @GetMapping(value = {"/get_node/{id}"})
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

    @GetMapping(value = {"/node_info_by_uuid"})
    public SethlansNode getNodeByUUID(@RequestParam String connection_uuid) {
        return sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
    }

    @GetMapping(value = {"/is_benchmark_complete"})
    public boolean isBenchmarkComplete(@RequestParam String connection_uuid) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
        return sethlansNode.isBenchmarkComplete();
    }

    @GetMapping(value = {"/node_list_size"})
    public Integer getNodeListSize() {
        return sethlansNodeDatabaseService.listAll().size();
    }


    @GetMapping(value = "/primary_blender_version")
    public Map primaryBlenderVersion() {
        return Collections.singletonMap("primary_blender",
                SethlansUtils.getProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION.toString()));
    }

    @GetMapping(value = "/remaining_blender_versions")
    public List<String> getNewBlenderVersions() {
        List<String> newBlenderVersions = new ArrayList<>();
        List<String> allSupportedVersions = BlenderUtils.listVersions();
        Set<String> installedVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        for (String version : allSupportedVersions) {
            if (!installedVersions.contains(version)) {
                newBlenderVersions.add(version);
            }
        }
        return newBlenderVersions;
    }

    @PostMapping(value = "/add_blender_version")
    public boolean addNewBlenderVersion(@RequestParam String version) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        Set<BlenderBinaryOS> blenderBinaryOSSet = new HashSet<>();
        if (sethlansNodeList.size() > 0) {
            for (SethlansNode sethlansNode : sethlansNodeList) {
                blenderBinaryOSSet.add(sethlansNode.getSethlansNodeOS());
            }
        } else {
            blenderBinaryOSSet.add(BlenderBinaryOS.valueOf(SethlansUtils.getOS()));
        }

        for (BlenderBinaryOS blenderBinaryOS : blenderBinaryOSSet) {
            BlenderBinary blenderBinary = new BlenderBinary();
            blenderBinary.setBlenderVersion(version);
            blenderBinary.setBlenderBinaryOS(blenderBinaryOS.toString());
            blenderBinaryDatabaseService.saveOrUpdate(blenderBinary);
        }
        return true;
    }

    @GetMapping(value = {"/node_check"})
    public SethlansNode checkNode(@RequestParam String ip, @RequestParam String port) {
        return nodeDiscoveryService.discoverUnicastNode(ip, port);
    }

    @GetMapping(value = "/is_key_present")
    public boolean isKeyPresentOnNode(@RequestParam String ip, @RequestParam String port) {
        String connection = "https://" + ip + ":" + port + "/api/info/check_key";
        String params = "check_key=" + SethlansUtils.getProperty(SethlansConfigKeys.ACCESS_KEY.toString());
        return sethlansAPIConnectionService.queryNode(connection, params);

    }

    @GetMapping(value = "/set_primary_blender_version")
    public boolean setPrimaryBlenderVersion(@RequestParam String version) {
        writeProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION, version);
        return true;
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
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }
}
