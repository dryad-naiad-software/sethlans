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
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * File created by Mario Estrella on 1/1/2021.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@Service
@Profile({"NODE", "DUAL"})
public class NodeServiceImpl implements NodeService {
    private final BenchmarkService benchmarkService;

    public NodeServiceImpl(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @Override
    public ResponseEntity<Void> incomingBenchmarkRequest(Server server) {
        log.debug("Incoming Benchmark Request from " + server);
        var objectMapper = new ObjectMapper();
        var nodeID = ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID);
        try {
            var path = "/api/v1/server_queue/latest_blender_archive?system-id=" + nodeID + "&os=" + QueryUtils.getOS().toString();
            var host = server.getIpAddress();
            var port = server.getNetworkPort();
            var blenderArchiveJSON = NetworkUtils.getJSONFromURL(path,host, port, true);
            log.debug(blenderArchiveJSON);
            var blenderArchive = objectMapper.readValue(blenderArchiveJSON, new TypeReference<BlenderArchive>() {
            });
            benchmarkService.processBenchmarkRequest(server, blenderArchive);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
