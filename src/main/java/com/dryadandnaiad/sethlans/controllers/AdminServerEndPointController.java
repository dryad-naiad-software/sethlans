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

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.forms.NodeForm;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Notification;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.NotificationRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.services.ServerQueueService;
import com.dryadandnaiad.sethlans.services.ServerService;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final ServerService serverService;
    private final NodeRepository nodeRepository;
    private final BlenderArchiveRepository blenderArchiveRepository;
    private final ServerQueueService serverQueueService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public AdminServerEndPointController(ServerService serverService, NodeRepository nodeRepository,
                                         BlenderArchiveRepository blenderArchiveRepository,
                                         ServerQueueService serverQueueService,
                                         NotificationRepository notificationRepository, UserRepository userRepository) {
        this.serverService = serverService;
        this.nodeRepository = nodeRepository;
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.serverQueueService = serverQueueService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/view_server_pending_queue")
    public List<RenderTask> viewServerQueue() {
        return serverQueueService.listCurrentTasksInQueue();
    }

    @GetMapping("/reset_server_pending_queue")
    public ResponseEntity<Void> resetServerQueue() {
        serverQueueService.resetPendingRenderTaskQueue();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/blender_download_complete")
    public boolean isBlenderDownloadComplete() {
        return !blenderArchiveRepository.findAllByDownloadedIsTrue().isEmpty();
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

    @GetMapping("/current_node_list")
    public List<Node> nodeList() {
        return nodeRepository.findAll();
    }

    @PostMapping("/add_nodes_to_server")
    public ResponseEntity<String> addNodes(@RequestBody List<NodeForm> selectedNodes) {
        log.info(selectedNodes.toString());
        return serverService.addNodes(selectedNodes);
    }

    @GetMapping("/node_benchmark_status")
    public boolean isNodeBenchmarkComplete(@RequestParam String nodeID) {
        if (nodeRepository.findNodeBySystemIDEquals(nodeID).isPresent()) {
            var nodeToCheck = nodeRepository.findNodeBySystemIDEquals(nodeID).get();
            var path = "/api/v1/management/benchmark_status";
            var host = nodeToCheck.getIpAddress();
            var port = nodeToCheck.getNetworkPort();
            var params = ImmutableMap.<String, String>builder()
                    .put("serverID", PropertiesUtils.getSystemID())
                    .build();
            return Boolean.parseBoolean(NetworkUtils.getJSONWithParams(path, host, port, params, true));
        }

        return false;
    }

    @PostMapping("/update_node_benchmark_state")
    public ResponseEntity<Void> updateNodeBenchmarkState(@RequestBody Node node) {
        var nodeID = node.getSystemID();
        var nodeType = node.getNodeType();
        if (nodeRepository.findNodeBySystemIDEquals(nodeID).isPresent()) {
            var nodeToSave = nodeRepository.findNodeBySystemIDEquals(nodeID).get();
            log.debug("Received Benchmark(s) from " + nodeToSave.getHostname() );

            switch (nodeType) {
                case CPU -> nodeToSave.setCpuRating(node.getCpuRating());
                case GPU -> nodeToSave.setSelectedGPUs(node.getSelectedGPUs());
                case CPU_GPU -> {
                    nodeToSave.setCpuRating(node.getCpuRating());
                    nodeToSave.setSelectedGPUs(node.getSelectedGPUs());
                }
            }
            nodeToSave.setBenchmarkPending(false);
            nodeToSave.setBenchmarkComplete(true);
            nodeToSave.setActive(true);
            nodeToSave.setTotalRenderingSlots(node.getTotalRenderingSlots());
            nodeRepository.save(nodeToSave);
            serverQueueService.updatePendingQueueLimit();
            var notification = Notification.builder()
                    .messageDate(LocalDateTime.now())
                    .message("Benchmarks have been received from " + nodeToSave.getHostname())
                    .build();
            var super_administrators = userRepository.findAllByRolesContaining(Role.SUPER_ADMINISTRATOR);
            var administrators = userRepository.findAllByRolesContaining(Role.ADMINISTRATOR);
            var admins = new LinkedHashSet<>(super_administrators);
            admins.addAll(administrators);
            for (User user : admins) {
                notification.setUserID(user.getUserID());
                notificationRepository.save(notification);
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
