/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
import com.google.common.base.Throwables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Value("${sethlans.cores}")
    String cores;

    @Value("${sethlans.primaryBlenderVersion}")
    private String primaryBlenderVersion;

    private static final Logger LOG = LoggerFactory.getLogger(BlenderBenchmarkServiceImpl.class);
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private BlenderPythonScriptService blenderPythonScriptService;
    private int remainingBenchmarks;

    @Override
    public void sendBenchmarktoNode(SethlansNode sethlansNode) {
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
                if (!benchmarkTask.isComplete()) {
                    pendingBenchmarks.add(benchmarkTask);
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
        this.remainingBenchmarks = benchmark_uuids.size();
        for (String benchmark_uuid : benchmark_uuids) {
            startBenchmarkService(benchmark_uuid);
        }
    }

    private void startBenchmarkService(String benchmark_uuid) {
        LOG.debug("Starting Benchmark");
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        benchmarkTask.setInProgress(true);
        runBenchmark(benchmarkTask);
        benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        if (benchmarkTask.isComplete()) {
            try {
                FileUtils.deleteDirectory(new File(benchmarkTask.getBenchmarkDir()));
                remainingBenchmarks--;
            } catch (IOException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
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
            String params = "connection_uuid=" + benchmarkTask.getConnection_uuid() + "&version=" + benchmarkTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
            String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            if (SethlansUtils.archiveExtract(filename, benchmarkDir)) {
                LOG.debug("Extraction complete.");
                if (SethlansUtils.renameBlender(benchmarkDir, benchmarkTask.getBlenderVersion())) {
                    LOG.debug("Blender executable ready");
                    benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
                    benchmarkTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(benchmarkDir));
                } else {
                    LOG.debug("Rename failed.");
                    return false;
                }
            }


            // Download benchmark from server
            connectionURL = "https://" + serverIP + ":" + serverPort + "/api/benchmark_files/" + benchmarkTask.getBenchmarkURL() + "/";
            params = "connection_uuid=" + benchmarkTask.getConnection_uuid();
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
            benchmarkTask = blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
            if (benchmarkTask.getComputeType().equals(ComputeType.GPU)) {
                LOG.debug("Creating benchmark script using " + benchmarkTask.getCudaName());
                String cudaID = StringUtils.substringAfter(benchmarkTask.getCudaName(), "_");
                String script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                        benchmarkTask.getBenchmarkDir(), cudaID, 128, 800, 600, 50);
                int rating = blenderBenchmark(benchmarkTask, script);
                if (rating == 0) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    LOG.debug(benchmarkTask.toString());
                    benchmarkTask.setGpuRating(rating);
                    benchmarkTask.setInProgress(false);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                }

            } else {
                LOG.debug("Creating benchmark script using CPU");
                String script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                        benchmarkTask.getBenchmarkDir(), "0", 16, 800, 600, 50);
                int rating = blenderBenchmark(benchmarkTask, script);
                if (rating == 0) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    benchmarkTask.setCpuRating(rating);
                    benchmarkTask.setInProgress(false);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                    LOG.debug(benchmarkTask.toString());
                }

            }
        }

    }

    private int blenderBenchmark(BlenderBenchmarkTask benchmarkTask, String blenderScript) {
        String error;
        try {
            LOG.debug("Starting Benchmark. Benchmark type: " + benchmarkTask.getComputeType());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(benchmarkTask.getBlenderExecutable());

            commandLine.addArgument("-b");
            commandLine.addArgument(benchmarkTask.getBenchmarkDir() + File.separator + benchmarkTask.getBenchmarkFile());
            commandLine.addArgument("-P");
            commandLine.addArgument(blenderScript);
            commandLine.addArgument("-E");
            commandLine.addArgument("CYCLES");
            commandLine.addArgument("-o");
            commandLine.addArgument(benchmarkTask.getBenchmarkDir() + File.separator);
            commandLine.addArgument("-f");
            commandLine.addArgument("1");
            if (benchmarkTask.getComputeType().equals(ComputeType.CPU)) {
                commandLine.addArgument("-t");
                commandLine.addArgument(cores);
            }
            LOG.debug(commandLine.toString());

            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(pumpStreamHandler);
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            executor.execute(commandLine, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output;
            String time = null;

            while ((output = in.readLine()) != null) {
                LOG.debug(output);
                if (output.contains("Finished")) {
                    String[] finished = output.split("\\|");
                    for (String item : finished) {
                        LOG.debug(item);
                        if (item.contains("Time:")) {
                            time = StringUtils.substringAfter(item, ":");
                            time = StringUtils.substringBefore(time, ".");
                        }
                    }
                }
            }


            error = errorStream.toString();

            LOG.debug(error);
            String[] timeToConvert = time.split(":");
            int minutes = Integer.parseInt(timeToConvert[0]);
            int seconds = Integer.parseInt(timeToConvert[1]);
            int timeInSeconds = seconds + 60 * minutes;
            int timeInMilliseconds = (int) TimeUnit.MILLISECONDS.convert(timeInSeconds, TimeUnit.SECONDS);
            LOG.debug("Benchmark time in milliseconds: " + timeInMilliseconds);
            return timeInMilliseconds;


        } catch (IOException | NullPointerException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        } catch (InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return 0;
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

}
