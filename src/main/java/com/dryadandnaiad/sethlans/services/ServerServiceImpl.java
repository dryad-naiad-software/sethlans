/*
 * Copyright (c) 2021. Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.forms.NodeForm;
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
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * File created by Mario Estrella on 1/1/2021.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
@Profile({"SERVER", "DUAL"})
public class ServerServiceImpl implements ServerService {
    private final NodeRepository nodeRepository;

    public ServerServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public ResponseEntity<String> addNodes(List<NodeForm> selectedNodes) {
        log.info("Adding Nodes: " + selectedNodes);
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
                if (nodeRepository.existsNodeByIpAddressAndNetworkPort(selectedNode.getIpAddress(),
                        selectedNode.getNetworkPort())) {
                    log.error("Error adding " + selectedNode.getIpAddress() + ":" +
                            selectedNode.getNetworkPort() +
                            " this node already exists on this server.");
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                var loginURL = "/login";
                var addServerURL = "/api/v1/management/add_server_to_node";
                var getSystemIDURL = "/api/v1/management/system_id";
                if (NetworkUtils.postJSONToURLWithAuth(selectedNode.getNetworkPort(), selectedNode.getIpAddress(),
                        addServerURL, true, serverAsJson, selectedNode.getUsername(),
                        selectedNode.getPassword())) {
                    var node = NetworkUtils.getNodeViaJson(selectedNode.getIpAddress(),
                            selectedNode.getNetworkPort());
                    var systemID = NetworkUtils.getJSONFromURLWithAuth(getSystemIDURL,
                            selectedNode.getIpAddress(), selectedNode.getNetworkPort(), true,
                            selectedNode.getUsername(), selectedNode.getPassword());
                    if (node != null && systemID != null) {
                        node.setSystemID(systemID);
                        log.debug("Adding the following node to server: " + node);
                        nodeRepository.save(node);
                    }
                } else {
                    log.error("Unable to add " + selectedNode.getIpAddress());
                    log.error("Error adding " + selectedNode.getIpAddress() + ":" +
                            selectedNode.getNetworkPort() +
                            "! Please check to see if node is active or correct credentials are being used.");
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
