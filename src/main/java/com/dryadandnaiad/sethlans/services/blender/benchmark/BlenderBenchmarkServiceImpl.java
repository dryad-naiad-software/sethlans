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

package com.dryadandnaiad.sethlans.services.blender.benchmark;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.services.blender.BlenderPythonScriptService;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.dryadandnaiad.sethlans.services.blender.benchmark.PrepareScripts.prepareScriptandExecute;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderBenchmarkServiceImpl implements BlenderBenchmarkService {

    @Value("${sethlans.configDir}")
    private String configDir;


    private static final Logger LOG = LoggerFactory.getLogger(BlenderBenchmarkServiceImpl.class);
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private BlenderPythonScriptService blenderPythonScriptService;
    private SethlansNotificationService sethlansNotificationService;
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;


    @Override
    public void sendBenchmarktoNode(SethlansNode sethlansNode) {
        String primaryBlenderVersion = blenderBinaryDatabaseService.getHighestVersion();
        String message = "Sending benchmark request to " + sethlansNode.getHostname();
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.NODE, message, NotificationScope.ADMIN);
        sethlansNotificationService.sendNotification(sethlansNotification);
        String nodeURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/benchmark/request";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid() + "&compute_type=" + sethlansNode.getComputeType() +
                "&blender_version=" + primaryBlenderVersion;
        sethlansAPIConnectionService.sendToRemotePOST(nodeURL, params);
    }

    @Async
    @Override
    public void benchmarkOnNodeRestart() {
        // If a node gets shutdown, this will attempt to process any pending benchmarks.
        try {
            Thread.sleep(10000);
            LOG.debug("Checking to see if any benchmarks are pending.");
            List<BlenderBenchmarkTask> blenderBenchmarkTaskList = blenderBenchmarkTaskDatabaseService.listAll();
            List<BlenderBenchmarkTask> pendingBenchmarks = new ArrayList<>();
            for (BlenderBenchmarkTask benchmarkTask : blenderBenchmarkTaskList) {
                if (!benchmarkTask.isComplete() && sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid()) != null) {
                    pendingBenchmarks.add(benchmarkTask);
                }
                if (!benchmarkTask.isComplete() && sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid()) == null) {
                    LOG.debug("Removing stale benchmarks.");
                    blenderBenchmarkTaskDatabaseService.deleteAllByConnection(benchmarkTask.getConnection_uuid());
                }
            }
            if (pendingBenchmarks.size() > 1) {
                LOG.debug("There are " + pendingBenchmarks.size() + " benchmarks pending.");
                List<String> benchmarkUUIDs = new ArrayList<>();
                for (BlenderBenchmarkTask pendingBenchmark : pendingBenchmarks) {
                    benchmarkUUIDs.add(pendingBenchmark.getBenchmark_uuid());
                }
                processReceivedBenchmarks(benchmarkUUIDs);
            } else if (pendingBenchmarks.size() == 1) {
                LOG.debug("There is one benchmark pending.");
                processReceivedBenchmark(pendingBenchmarks.get(0).getBenchmark_uuid());
            } else {
                LOG.debug("No benchmarks are pending.");
            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting down Benchmark Service");
        }

    }

    @Override
    @Async
    public void processReceivedBenchmark(String benchmark_uuid) {
            startBenchmarkService(benchmark_uuid);
    }

    @Override
    @Async
    public void processReceivedBenchmarks(List<String> benchmark_uuids) {
            for (String benchmark_uuid : benchmark_uuids) {
                startBenchmarkService(benchmark_uuid);
            }
    }

    private void startBenchmarkService(String benchmark_uuid) {
        LOG.debug("Starting Benchmark");
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        benchmarkTask.setInProgress(true);
        prepareScriptandExecute(benchmarkTask, sethlansAPIConnectionService, sethlansServerDatabaseService, blenderBenchmarkTaskDatabaseService, blenderPythonScriptService);
        benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        if (benchmarkTask.isComplete()) {
            try {
                FileUtils.deleteDirectory(new File(benchmarkTask.getBenchmarkDir()));
            } catch (IOException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
            int count = 0;
            while (true) {
                if (sendResultsToServer(benchmarkTask.getConnection_uuid(), benchmarkTask)) {
                    break;
                }
                if (count >= 10) {
                    LOG.debug("Unable to establish a connection with the server to send results.");
                    break;
                }
                try {
                    Thread.sleep(5000);
                    count++;
                } catch (InterruptedException e) {
                    LOG.error(Throwables.getStackTraceAsString(e));
                }
            }
        }
    }

    private boolean sendResultsToServer(String connectionUUID, BlenderBenchmarkTask blenderBenchmarkTask) {
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connectionUUID);
        String message = "Sending " + blenderBenchmarkTask.getComputeType() + " benchmark result to " + sethlansServer.getHostname();
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SERVER, message, NotificationScope.ADMIN);
        sethlansNotificationService.sendNotification(sethlansNotification);
        blenderBenchmarkTaskDatabaseService.delete(blenderBenchmarkTask.getId());
        LOG.debug("Remaining benchmarks to process: " + blenderBenchmarkTaskDatabaseService.tableSize());
        boolean complete = blenderBenchmarkTaskDatabaseService.tableSize() <= 0;
        String serverUrl = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/benchmark/response";
        String params;
        if (blenderBenchmarkTask.getComputeType().equals(ComputeType.CPU)) {
            params = "connection_uuid=" + sethlansServer.getConnection_uuid() + "&rating=" + blenderBenchmarkTask.getCpuRating() + "&cuda_name=" + "&compute_type=" +
                    blenderBenchmarkTask.getComputeType() + "&complete=" + complete;

        } else {
            params = "connection_uuid=" + sethlansServer.getConnection_uuid() + "&rating=" + blenderBenchmarkTask.getGpuRating() + "&cuda_name=" + blenderBenchmarkTask.getDeviceID() + "&compute_type=" +
                    blenderBenchmarkTask.getComputeType() + "&complete=" + complete;
        }
        return sethlansAPIConnectionService.sendToRemotePOST(serverUrl, params);
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setBlenderPythonScriptService(BlenderPythonScriptService blenderPythonScriptService) {
        this.blenderPythonScriptService = blenderPythonScriptService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }
}