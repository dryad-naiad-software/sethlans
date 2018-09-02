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
import com.dryadandnaiad.sethlans.domains.info.GettingStartedInfo;
import com.dryadandnaiad.sethlans.domains.info.NodeItem;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;
import static io.restassured.RestAssured.given;

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
    private static final Logger LOG = LoggerFactory.getLogger(AdminServerController.class);
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
        List<String> listOfVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        return Collections.singletonMap("installedBlenderVersions", listOfVersions);
    }

    @GetMapping(value = "/get_blender_list")
    public List<BlenderBinaryInfo> getBlenderBinaryInfoList() {
        List<BlenderBinaryInfo> blenderBinaryInfoList = new ArrayList<>();
        List<String> listOfVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        for (String version : listOfVersions) {
            BlenderBinaryInfo blenderBinaryInfo = new BlenderBinaryInfo();
            blenderBinaryInfo.setVersion(version);
            blenderBinaryInfo.setBinaryOSList(blenderBinaryOSList(version));
            blenderBinaryInfoList.add(blenderBinaryInfo);
        }
        return blenderBinaryInfoList;
    }


    @GetMapping(value = "/get_key_from_server")
    public String getAccessKeyFromServer() {
        return getProperty(SethlansConfigKeys.ACCESS_KEY);
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
    public long getNodeListSize() {
        return sethlansNodeDatabaseService.tableSize();
    }


    @GetMapping(value = "/remaining_blender_versions")
    public List<String> getNewBlenderVersions() {
        List<String> newBlenderVersions = new ArrayList<>();
        List<String> allSupportedVersions = BlenderUtils.listVersions();
        List<String> installedVersions = blenderBinaryDatabaseService.installedBlenderVersions();
        for (String version : allSupportedVersions) {
            if (!installedVersions.contains(version)) {
                newBlenderVersions.add(version);
            }
        }
        return newBlenderVersions;
    }

    @PostMapping(value = "/server_to_node_auth")
    public boolean serverToNodeAuth(HttpEntity<String> httpEntity) {
        String json = httpEntity.getBody();
        Gson gson = new Gson();
        String accessKey = getAccessKeyFromServer();
        GettingStartedInfo gettingStartedInfo = gson.fromJson(json, GettingStartedInfo.class);
        for (NodeItem node : gettingStartedInfo.getListOfNodes()) {
            RestAssured.useRelaxedHTTPSValidation();
            RestAssured.baseURI = "https://" + node.getIpAddress();
            RestAssured.port = node.getPort();
            RestAssured.basePath = "/";
            Response response =
                    given().
                            when().get("/login").
                            then().extract().response();
            String token = response.cookie("XSRF-TOKEN");

            response = given().log().ifValidationFails()
                    .header("X-XSRF-TOKEN", token)
                    .cookie("XSRF-TOKEN", token).param("username", gettingStartedInfo.getLogin().getUsername().toLowerCase()).param("password", gettingStartedInfo.getLogin().getPassword())
                    .when().post("/login").then().statusCode(302).extract().response();

            RestAssured.sessionId = response.cookie("JSESSIONID");

            given().log().ifValidationFails()
                    .header("X-XSRF-TOKEN", token)
                    .cookie("XSRF-TOKEN", token).param("access_key", accessKey)
                    .when().post("/api/setup/add_access_key");
        }

        return true;
    }




    @GetMapping(value = {"/node_check"})
    public SethlansNode checkNode(@RequestParam String ip, @RequestParam String port) {
        return nodeDiscoveryService.discoverUnicastNode(ip, port);
    }

    @GetMapping(value = "/is_key_present")
    public boolean isKeyPresentOnNode(@RequestParam String ip, @RequestParam String port) {
        LOG.debug("Checking key");
        String connection = "https://" + ip + ":" + port + "/api/info/check_key/";
        String params = "access_key=" + getProperty(SethlansConfigKeys.ACCESS_KEY);
        return sethlansAPIConnectionService.queryNode(connection, params);

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
