/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.forms.NodeForm;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * File created by Mario Estrella on 6/12/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/v1/management")
@Profile({"SERVER", "DUAL"})
@Slf4j
public class AdminServerEndPointController {

    private final NodeRepository nodeRepository;

    public AdminServerEndPointController(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @GetMapping("/network_node_scan")
    public Set<Node> nodeScan() {
        return NetworkUtils.discoverNodesViaMulticast();
    }

    @GetMapping("/retrieve_node_detail_list")
    public Set<Node> nodesSet(@RequestBody List<NodeForm> nodes) {
        var nodeSet = new HashSet<Node>();
        for (NodeForm node : nodes) {
            var retrievedNode = NetworkUtils.getNodeViaJson(node.getIpAddress(), node.getNetworkPort());
            if (retrievedNode != null) {
                nodeSet.add(retrievedNode);
            }
        }
        return nodeSet;
    }

    @PostMapping("/add_nodes_to_server")
    public ResponseEntity<Void> addNodes(@RequestBody List<NodeForm> selectedNodes) {
        var count = nodeRepository.count();
        try {
            var server = Server.builder()
                    .hostname(QueryUtils.getHostname())
                    .ipAddress(QueryUtils.getIP())
                    .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                    .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                    .build();
            var objectMapper = new ObjectMapper();

            var serverAsJson = objectMapper.writeValueAsString(server);

            for (NodeForm selectedNode : selectedNodes) {
                var loginURL = new URL("https://" + selectedNode.getIpAddress() + ":" +
                        selectedNode.getNetworkPort() + "/login");
                var addServerURL = new URL("https://" + selectedNode.getIpAddress() + ":" +
                        selectedNode.getNetworkPort() + "/api/v1/management/add_server_to_node");
                var getSystemIDURL = new URL("https://" + selectedNode.getIpAddress() + ":" +
                        selectedNode.getNetworkPort() + "/api/v1/management/system_id");
                if (NetworkUtils.postJSONToURLWithAuth(loginURL,
                        addServerURL, serverAsJson, selectedNode.getUsername(),
                        selectedNode.getPassword()).equals(HttpStatus.CREATED)) {
                    var node = NetworkUtils.getNodeViaJson(selectedNode.getIpAddress(), selectedNode.getNetworkPort());
                    var systemID = NetworkUtils.getJSONFromURLWithAuth(loginURL,
                            getSystemIDURL, selectedNode.getUsername(),
                            selectedNode.getPassword());
                    if (node != null && systemID != null) {
                        node.setSystemID(systemID);
                        log.debug("Adding the following node to server: " + node);
                        nodeRepository.save(node);
                    }
                }
            }
            if (nodeRepository.count() > count) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (JsonProcessingException | MalformedURLException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

}
