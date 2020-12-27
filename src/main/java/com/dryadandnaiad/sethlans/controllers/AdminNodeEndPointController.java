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
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.services.BenchmarkService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * File created by Mario Estrella on 6/14/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/management")
@Profile({"NODE", "DUAL"})
public class AdminNodeEndPointController {

    private final ServerRepository serverRepository;
    private final BenchmarkService benchmarkService;

    public AdminNodeEndPointController(ServerRepository serverRepository, BenchmarkService benchmarkService) {
        this.serverRepository = serverRepository;
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/add_server_to_node")
    public ResponseEntity<Void> addServer(@RequestBody Server server) {
        if (serverRepository.findBySystemID(server.getSystemID()).isEmpty()) {
            log.debug("Adding the following server to node: " + server);
            serverRepository.save(server);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        log.error("Server already exists on this node.");
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    @GetMapping("/list_servers_on_node")
    public List<Server> servers() {
        return serverRepository.findAll();
    }

    @GetMapping("/benchmark_request")
    public ResponseEntity<Void> requestBenchmark(@RequestBody Server server) {
        if (serverRepository.findBySystemID(server.getSystemID()).isPresent()) {
            var objectMapper = new ObjectMapper();
            var nodeID = ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID);
            try {
                var url = new URL("https://" + server.getIpAddress() + ":" + server.getNetworkPort()
                        + "/latest_blender_archive?system-id=" + nodeID + "&os=" + QueryUtils.getOS().getName());
                var blenderArchiveJSON = NetworkUtils.getJSONFromURL(url);
                var blenderArchive = objectMapper.readValue(blenderArchiveJSON, new TypeReference<BlenderArchive>() {
                });
                benchmarkService.processBenchmarkRequest(server, blenderArchive);
            } catch (JsonProcessingException | MalformedURLException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        log.error("Server is not authorized on this node.");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/benchmark_status")
    public boolean benchmarkStatus(@RequestBody Server server) {
        if (serverRepository.findBySystemID(server.getSystemID()).isPresent()) {
            return benchmarkService.benchmarkStatus(server);
        }
        log.error("Server is not authorized on this node.");
        return false;
    }

    @GetMapping("/node_disabled")
    public boolean isNodeDisabled() {
        return PropertiesUtils.isNodeDisabled();
    }


}