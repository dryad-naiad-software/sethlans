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
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.services.BenchmarkService;
import com.dryadandnaiad.sethlans.services.NodeService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * File created by Mario Estrella on 6/14/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/management/")
@Profile({"NODE", "DUAL"})
public class AdminNodeEndPointController {

    private final BenchmarkService benchmarkService;
    private final NodeService nodeService;

    public AdminNodeEndPointController(BenchmarkService benchmarkService, NodeService nodeService) {
        this.benchmarkService = benchmarkService;
        this.nodeService = nodeService;
    }

    @PostMapping("/add_server_to_node")
    public ResponseEntity<Void> addServer(@RequestBody Server server) {
        var apiKey = ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY);
        if (apiKey == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            if (server.getApiKey().equals(apiKey)) {
                PropertiesUtils.updatedAuthorizedServer(server);
                return new ResponseEntity<>(HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
    }

    @GetMapping("/node_api_key")
    public Map<String, String> nodeAPIKey() {
        var apiKey = new HashMap<String, String>();
        apiKey.put("api_key", ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY));
        return apiKey;
    }

    @GetMapping("/authorized_server_on_node")
    public Server authorizedServer() {
        return PropertiesUtils.getAuthorizedServer();

    }

    @PostMapping("/benchmark_request")
    public ResponseEntity<Void> incomingBenchmarkRequest(@RequestBody Server server) {
        var authorizedServer = PropertiesUtils.getAuthorizedServer();
        if (authorizedServer != null && authorizedServer.getSystemID().equals(server.getSystemID())) {
            return nodeService.incomingBenchmarkRequest(server);
        }

        log.error("Server is not authorized on this node.");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/benchmark_status")
    public boolean benchmarkStatus(@RequestParam String serverID) {
        var authorizedServer = PropertiesUtils.getAuthorizedServer();
        if (authorizedServer != null && authorizedServer.getSystemID().equals(serverID)) {
            return benchmarkService.benchmarksComplete(authorizedServer);
        }
        log.error("Server is not authorized on this node.");
        return false;
    }

    @GetMapping("/is_node_paused")
    public boolean isNodePaused() {
        return PropertiesUtils.isNodePaused();
    }

    @PostMapping("/pause_node")
    public ResponseEntity<Void> pauseNode(@RequestParam("pause") boolean pause) {
        log.info("Changing node status.");
        if (pause) {
            log.info("Node is paused.");
        } else {
            log.info("Node is not paused.");
        }
        try {
            ConfigUtils.writeProperty(ConfigKeys.NODE_PAUSED, Boolean.toString(pause));
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/set_node_api_key")
    public ResponseEntity<Void> setNodeAPIKey(@RequestParam("api-key") String apiKey) {
        try {
            ConfigUtils.writeProperty(ConfigKeys.SETHLANS_API_KEY, apiKey);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get_node_settings")
    public NodeSettings getNodeSettings() {
        var systemInfo = QueryUtils.getCurrentSystemInfo();
        return NodeSettings.builder()
                .nodeType(PropertiesUtils.getNodeType())
                .availableTypes(QueryUtils.getAvailableTypes())
                .gpuCombined(PropertiesUtils.isGPUCombined())
                .availableGPUs(systemInfo.getGpuList())
                .totalCores(systemInfo.getCpu().getCores())
                .tileSizeCPU(PropertiesUtils.getCPUTileSize())
                .cores(PropertiesUtils.getSelectedCores())
                .tileSizeGPU(PropertiesUtils.getGPUTileSize())
                .selectedGPUs(PropertiesUtils.getSelectedGPUs())
                .build();
    }

    @PostMapping("/change_node_settings")
    public ResponseEntity<Void> changeNodeSettings(@RequestBody NodeSettings nodeSettings) {
        try {
            PropertiesUtils.writeNodeSettings(nodeSettings);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
