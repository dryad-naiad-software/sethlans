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
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderBenchmarkServiceImpl implements BlenderBenchmarkService {
    @Value("${sethlans.tempDir}")
    private String tempDir;

    @Value("${sethlans.primaryBlenderVersion}")
    private String primaryBlenderVersion;
    private int remainingBenchmarks;

    private static final Logger LOG = LoggerFactory.getLogger(BlenderBenchmarkServiceImpl.class);

    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private BlenderPythonScriptService blenderPythonScriptService;
    private BlenderRenderService blenderRenderService;

    @Override
    public void sendBenchmarktoNode(SethlansNode sethlansNode) {
        String nodeURL = "https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() + "/api/benchmark/request";
        String params = "connection_uuid=" + sethlansNode.getConnection_uuid() + "&compute_type=" + sethlansNode.getComputeType() +
                "&blender_version=" + primaryBlenderVersion;
        sethlansAPIConnectionService.sendToRemotePOST(nodeURL, params);

    }

    @Override
    @Async
    public void processReceivedBenchmark(String benchmark_uuid) {
        startBenchmarkService(benchmark_uuid);
    }

    @Override
    @Async
    public void processReceivedBenchmarks(List<String> benchmark_uuids) {
        this.remainingBenchmarks = benchmark_uuids.size();
        for (String benchmark_uuid : benchmark_uuids) {
            startBenchmarkService(benchmark_uuid);
        }
    }

    private void startBenchmarkService(String benchmark_uuid) {
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        runBenchmark(benchmarkTask);
        benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        if (benchmarkTask.isComplete()) {
            try {
                FileUtils.deleteDirectory(new File(benchmarkTask.getBenchmarkDir()));
                remainingBenchmarks--;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendResultsToServer(benchmarkTask.getConnection_uuid(), benchmarkTask);
        }

    }

    private void sendResultsToServer(String connectionUUID, BlenderBenchmarkTask blenderBenchmarkTask) {
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connectionUUID);
        boolean complete;
        LOG.debug("Remaining benchmarks to process: " + remainingBenchmarks);
        if (remainingBenchmarks > 0) {
            complete = false;
        } else {
            complete = true;
        }
        String serverUrl = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/benchmark/response";
        String params;
        if (blenderBenchmarkTask.getComputeType().equals(ComputeType.CPU)) {
            params = "connection_uuid=" + sethlansServer.getConnection_uuid() + "&rating=" + blenderBenchmarkTask.getCpuRating() + "&cuda_name=" + "&compute_type=" +
                    blenderBenchmarkTask.getComputeType() + "&complete=" + complete;

        } else {
            params = "connection_uuid=" + sethlansServer.getConnection_uuid() + "&rating=" + blenderBenchmarkTask.getGpuRating() + "&cuda_name=" + blenderBenchmarkTask.getCudaName() + "&compute_type=" +
                    blenderBenchmarkTask.getComputeType() + "&complete=" + complete;
        }
        sethlansAPIConnectionService.sendToRemotePOST(serverUrl, params);
    }


    private boolean downloadRequiredFiles(File benchmarkDir, BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();

        if (benchmarkDir.mkdirs()) {

            //Download Blender from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
            String params = "?connection_uuid=" + benchmarkTask.getConnection_uuid() + "&version=" + benchmarkTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
            String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            if (SethlansUtils.archiveExtract(filename, benchmarkDir)) {
                if (SethlansUtils.renameBlender(benchmarkDir, primaryBlenderVersion)) {
                    LOG.debug("Blender executable extracted and ready");
                    benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
                    benchmarkTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(benchmarkDir));
                } else {
                    return false;
                }
            }


            // Download benchmark from server
            connectionURL = "https://" + serverIP + ":" + serverPort + "/api/benchmark_files/" + benchmarkTask.getBenchmarkURL();
            params = "?connection_uuid=" + benchmarkTask.getConnection_uuid();
            String benchmarkFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            benchmarkTask.setBenchmarkFile(benchmarkFile);
            LOG.debug("Required files downloaded.");
            return true;

        }
        return false;
    }




    private void runBenchmark(BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Processing benchmark task: \n" + benchmarkTask.toString());
        File benchmarkDir = new File(tempDir + File.separator + benchmarkTask.getBenchmark_uuid() + "_" + benchmarkTask.getBenchmarkURL());
        if (downloadRequiredFiles(benchmarkDir, benchmarkTask)) {
            blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
            String uuid = benchmarkTask.getBenchmark_uuid();
            benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(uuid);
            if (benchmarkTask.getComputeType().equals(ComputeType.GPU)) {
                LOG.debug("Creating benchmark script using " + benchmarkTask.getCudaName());
                String cudaID = StringUtils.substringAfter(benchmarkTask.getCudaName(), "_");
                String script = blenderPythonScriptService.writePythonScript(benchmarkTask.getComputeType(),
                        benchmarkTask.getBenchmarkDir(), cudaID, 128, 800, 600, 50);
                int rating = blenderRenderService.executeBenchmarkTask(benchmarkTask, script);
                if (rating == 0) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    LOG.debug(benchmarkTask.toString());
                    benchmarkTask.setGpuRating(rating);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                }

            } else {
                LOG.debug("Creating benchmark script using CPU");
                String script = blenderPythonScriptService.writePythonScript(benchmarkTask.getComputeType(),
                        benchmarkTask.getBenchmarkDir(), "0", 16, 800, 600, 50);
                int rating = blenderRenderService.executeBenchmarkTask(benchmarkTask, script);
                if (rating == 0) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    benchmarkTask.setCpuRating(rating);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                    LOG.debug(benchmarkTask.toString());
                }

            }
        }

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
    public void setBlenderRenderService(BlenderRenderService blenderRenderService) {
        this.blenderRenderService = blenderRenderService;
    }
}
