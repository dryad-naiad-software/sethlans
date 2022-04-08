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
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
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
    private final BlenderArchiveRepository blenderArchiveRepository;

    public ServerServiceImpl(NodeRepository nodeRepository, BlenderArchiveRepository blenderArchiveRepository) {
        this.nodeRepository = nodeRepository;
        this.blenderArchiveRepository = blenderArchiveRepository;
    }

    @Override
    public ResponseEntity<String> addNodes(List<NodeForm> selectedNodes) {
        log.info("Adding Nodes: " + selectedNodes);
        try {
            var server = Server.builder()
                    .hostname(QueryUtils.getHostname())
                    .ipAddress(QueryUtils.getIP())
                    .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                    .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                    .apiKey(ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY))
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
                var addServerURL = "/api/v1/management/add_server_to_node";
                var getSystemIDURL = "/api/v1/management/system_id";
                if (NetworkUtils.postJSONToURL(addServerURL, selectedNode.getIpAddress(), selectedNode.getNetworkPort(), serverAsJson, true)) {
                    var node = NetworkUtils.getNodeViaJson(selectedNode.getIpAddress(),
                            selectedNode.getNetworkPort());
                    var params = ImmutableMap.<String, String>builder()
                            .put("apiKey", ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY))
                            .build();
                    var systemID = NetworkUtils.getJSONWithParams(getSystemIDURL,
                            selectedNode.getIpAddress(), selectedNode.getNetworkPort(), params, true);
                    if (node != null && systemID != null) {
                        node.setSystemID(systemID);
                        log.debug("Adding the following node to server: " + node);
                        nodeRepository.save(node);
                        var checkBenchmark = new Thread(() -> {
                            pendingBenchmarksToSend();
                        });
                        checkBenchmark.start();
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

    @Override
    @Async
    public void pendingBenchmarksToSend() {
        if(blenderArchiveRepository.findAllByDownloadedIsTrue().isEmpty()) {
            try {
                Thread.sleep(300000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        } else {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }
        log.info("Checking to see if any nodes are pending a benchmark.");
        var nodesToBenchmark =
                nodeRepository.findNodesByBenchmarkCompleteFalseAndBenchmarkPendingFalse();
        try {
            var server = Server.builder()
                    .hostname(QueryUtils.getHostname())
                    .ipAddress(QueryUtils.getIP())
                    .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                    .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                    .build();

            var objectMapper = new ObjectMapper();
            var serverAsJson = objectMapper.writeValueAsString(server);

            if (nodesToBenchmark.size() > 0) {
                log.info(nodesToBenchmark.size() + " nodes need to be benchmarked.");
                for (Node node : nodesToBenchmark) {
                    var path = "/api/v1/management/benchmark_request";
                    var host = node.getIpAddress();
                    var port = node.getNetworkPort();
                    if (NetworkUtils.postJSONToURL(path, host, port, serverAsJson, true)) {
                        node.setBenchmarkPending(true);
                        nodeRepository.save(node);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
    }

}
