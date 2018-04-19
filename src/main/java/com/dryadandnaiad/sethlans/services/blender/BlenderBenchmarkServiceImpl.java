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
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
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

    @Value("${sethlans.binDir}")
    private String binDir;


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
            Thread.sleep(30000);
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
        this.remainingBenchmarks = benchmark_uuids.size();
        for (String benchmark_uuid : benchmark_uuids) {
            startBenchmarkService(benchmark_uuid);
        }
    }

    private void startBenchmarkService(String benchmark_uuid) {
        LOG.debug("Starting Benchmark");
        BlenderBenchmarkTask benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        benchmarkTask.setInProgress(true);
        prepareScriptandExecute(benchmarkTask);
        benchmarkTask = blenderBenchmarkTaskDatabaseService.getByBenchmarkUUID(benchmark_uuid);
        if (benchmarkTask.isComplete()) {
            try {
                FileUtils.deleteDirectory(new File(benchmarkTask.getBenchmarkDir()));
                remainingBenchmarks--;
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
        boolean complete;
        LOG.debug("Remaining benchmarks to process: " + remainingBenchmarks);
        complete = remainingBenchmarks <= 0;
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

    private boolean downloadRequiredFiles(File benchmarkDir, BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(benchmarkTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();
        String cachedBlenderBinaries = SethlansUtils.getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES.toString());

        if (benchmarkDir.mkdirs()) {

            String[] cachedBinariesList;
            boolean versionCached = false;
            cachedBinariesList = cachedBlenderBinaries.split(",");
            for (String binary : cachedBinariesList) {
                if (binary.equals(benchmarkTask.getBlenderVersion())) {
                    versionCached = true;
                    LOG.debug(binary + " renderer is already cached.  Skipping Download.");

                }
            }
            if (!versionCached) {
                //Download Blender from server
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
                String params = "connection_uuid=" + benchmarkTask.getConnection_uuid() + "&version=" + benchmarkTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
                String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);
                if (SethlansUtils.archiveExtract(filename, new File(binDir))) {
                    LOG.debug("Extraction complete.");
                    LOG.debug("Attempting to rename blender directory. Will attempt 3 tries.");
                    int count = 0;
                    while (!SethlansUtils.renameBlenderDir(benchmarkDir, new File(binDir), benchmarkTask, cachedBlenderBinaries)) {
                        count++;
                        if (count == 2) {
                            return false;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
                benchmarkTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(new File(binDir), benchmarkTask.getBlenderVersion()));
            }




            // Download benchmark from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/benchmark_files/" + benchmarkTask.getBenchmarkURL() + "/";
            String params = "connection_uuid=" + benchmarkTask.getConnection_uuid();
            String benchmarkFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, benchmarkDir.toString());
            benchmarkTask.setBenchmarkFile(benchmarkFile);
            LOG.debug("Required files downloaded.");
            return true;

        }
        return false;
    }

    private void prepareScriptandExecute(BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Processing benchmark task: " + benchmarkTask.toString());
        File benchmarkDir = new File(tempDir + File.separator + benchmarkTask.getBenchmark_uuid() + "_" + benchmarkTask.getBenchmarkURL());
        if (downloadRequiredFiles(benchmarkDir, benchmarkTask)) {
            benchmarkTask = blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);

            if (benchmarkTask.getComputeType().equals(ComputeType.GPU)) {
                LOG.debug("Creating benchmark script using " + benchmarkTask.getDeviceID());
                String deviceID = StringUtils.substringAfter(benchmarkTask.getDeviceID(), "_");
                String script = null;
                if (SethlansUtils.isCuda(benchmarkTask.getDeviceID())) {
                    LOG.debug("CUDA Device found, using cuda parameters for script");
                    script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                            benchmarkTask.getBenchmarkDir(), deviceID, true, "128", 800, 600, 50);
                } else {
                    LOG.debug("OpenCL Device found, using opencl parameters for script");

                    script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                            benchmarkTask.getBenchmarkDir(), deviceID, false, "128", 800, 600, 50);
                }

                int rating = executeBlenderBenchmark(benchmarkTask, script);
                if (rating == -1) {
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
                        benchmarkTask.getBenchmarkDir(), "0", false, "16", 800, 600, 50);
                int rating = executeBlenderBenchmark(benchmarkTask, script);
                if (rating == -1) {
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

    private int executeBlenderBenchmark(BlenderBenchmarkTask benchmarkTask, String blenderScript) {
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

            LOG.debug("Benchmark Output");
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
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(errorStream.toByteArray())));


            LOG.debug("Error Output");
            while ((error = errorIn.readLine()) != null) {
                LOG.debug(error);
            }

            String[] timeToConvert;
            if (time != null) {
                timeToConvert = time.split(":");
                int minutes = Integer.parseInt(timeToConvert[0]);
                int seconds = Integer.parseInt(timeToConvert[1]);
                int timeInSeconds = seconds + 60 * minutes;
                int timeInMilliseconds = (int) TimeUnit.MILLISECONDS.convert(timeInSeconds, TimeUnit.SECONDS);
                LOG.debug("Benchmark time in milliseconds: " + timeInMilliseconds);
                return timeInMilliseconds;
            }


        } catch (IOException | NullPointerException | InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        }
        return -1;
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
