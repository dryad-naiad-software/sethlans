/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Profile({"SERVER", "NODE", "DUAL"})
public class BlenderBenchmarkServiceImpl implements BlenderBenchmarkService {
    @Value("${sethlans.tempDir}")
    private String tempDir;

    @Value("${sethlans.primaryBlenderVersion}")
    private String primaryBlenderVersion;

    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;

    @Override
    public void sendBenchmarktoNode(SethlansNode sethlansNode) {
        String nodeURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/benchmark/request";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid() + "&compute_type=" + sethlansNode.getComputeType() + "&blender_version=" + primaryBlenderVersion;
        sethlansAPIConnectionService.sendToRemotePOST(nodeURL, params);

    }

    @Override
    public void processReceivedBenchmark(String benchmark_uuid) {
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        // Process benchmark

    }

    @Override
    public void processReceivedBenchmarks(List<String> benchmark_uuid) {
        for (String benchmark : benchmark_uuid) {
            BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark);
            // Process benchmark;
        }

    }

    @Override
    public void sendResultsToServer(SethlansServer sethlansServer) {

    }

    private boolean downloadRequiredFiles() {
        return false;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
    }
}
