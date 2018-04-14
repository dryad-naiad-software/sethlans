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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created Mario Estrella on 12/18/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderRenderServiceImpl implements BlenderRenderService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderRenderServiceImpl.class);
    @Value("${sethlans.cores}")
    String cores;

    @Value("${sethlans.cacheDir}")
    private String cacheDir;

    @Value("${sethlans.binDir}")
    private String binDir;

    @Value("${sethlans.gpu_id}")
    private String deviceID;

    @Value("${sethlans.tileSizeCPU}")
    private String tileSizeCPU;

    @Value("${sethlans.tileSizeGPU}")
    private String tileSizeGPU;


    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;
    private BlenderPythonScriptService blenderPythonScriptService;

    @Override
    @Async
    public void clearQueueOnNodeRestart() {
        // If a node gets shutdown, this will attempt to process any pending benchmarks.
        try {
            Thread.sleep(30000);
            LOG.debug("Checking to see if any render tasks are pending.");
            List<BlenderRenderTask> blenderRenderTaskList = blenderRenderTaskDatabaseService.listAll();
            if (blenderRenderTaskList.size() > 0) {
                LOG.debug("Clearing all Render Tasks in the database");
                blenderRenderTaskDatabaseService.deleteAll();
            } else {
                LOG.debug("No render tasks are pending.");
            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting down Render Service");
        }
    }

    @Override
    @Async
    public void startRenderService(String projectUUID) {
        BlenderRenderTask blenderRenderTask = blenderRenderTaskDatabaseService.getByProjectUUID(projectUUID);
        blenderRenderTask.setInProgress(true);
        File renderDir = new File(cacheDir + File.separator + blenderRenderTask.getBlenderFramePart().getPartFilename());
        if (downloadRequiredFiles(renderDir, blenderRenderTask)) {
            blenderRenderTask = blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);
            if (blenderRenderTask.getComputeType().equals(ComputeType.GPU)) {
                boolean isCuda = false;
                List<String> deviceList = Arrays.asList(deviceID.split(","));
                List<String> deviceIDList = new ArrayList<>();
                LOG.debug("Running render task using " + deviceID);
                for (String device : deviceList) {
                    deviceIDList.add(StringUtils.substringAfter(device, "_"));
                    isCuda = SethlansUtils.isCuda(device);
                }
                String script = blenderPythonScriptService.writeRenderPythonScript(blenderRenderTask.getComputeType(),
                        blenderRenderTask.getRenderDir(), deviceIDList,
                        getUnselectedIds(deviceList), isCuda,
                        blenderRenderTask.getRenderOutputFormat(),
                        tileSizeGPU,
                        blenderRenderTask.getTaskResolutionX(),
                        blenderRenderTask.getTaskResolutionY(),
                        blenderRenderTask.getPartResPercentage(),
                        blenderRenderTask.getSamples(),
                        blenderRenderTask.getBlenderFramePart().getPartPositionMaxY(),
                        blenderRenderTask.getBlenderFramePart().getPartPositionMinY());
                saveOnSuccess(blenderRenderTask, script);

            } else {
                LOG.debug("Running render task using CPU");
                List<String> emptyList = new ArrayList<>();
                String script = blenderPythonScriptService.writeRenderPythonScript(blenderRenderTask.getComputeType(),
                        blenderRenderTask.getRenderDir(), emptyList,
                        emptyList, false, blenderRenderTask.getRenderOutputFormat(), tileSizeCPU,
                        blenderRenderTask.getTaskResolutionX(),
                        blenderRenderTask.getTaskResolutionY(),
                        blenderRenderTask.getPartResPercentage(),
                        blenderRenderTask.getSamples(),
                        blenderRenderTask.getBlenderFramePart().getPartPositionMaxY(),
                        blenderRenderTask.getBlenderFramePart().getPartPositionMinY());
                saveOnSuccess(blenderRenderTask, script);
            }
        }
    }

    private void saveOnSuccess(BlenderRenderTask blenderRenderTask, String script) {
        Long renderTime = executeRenderTask(blenderRenderTask, script);
        if (renderTime != -1L) {
            LOG.debug("Render Successful! Updating task status.");
            blenderRenderTask.setInProgress(false);
            blenderRenderTask.setComplete(true);
            blenderRenderTask.setRenderTime(renderTime);
            blenderRenderTask = blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);
            int count = 0;
            while (true) {
                if (sendResultsToServer(blenderRenderTask.getConnection_uuid(), blenderRenderTask)) {
                    try {
                        LOG.debug("Cleaning up " + blenderRenderTask.getRenderDir());
                        FileUtils.deleteDirectory(new File(blenderRenderTask.getRenderDir()));
                        blenderRenderTaskDatabaseService.delete(blenderRenderTask);
                        break;
                    } catch (IOException e) {
                        LOG.error(Throwables.getStackTraceAsString(e));
                    }
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

    private boolean sendResultsToServer(String connectionUUID, BlenderRenderTask blenderRenderTask) {
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connectionUUID);
        String serverUrl = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/response";
        Map<String, String> params = new HashMap<>();
        params.put("connection_uuid", blenderRenderTask.getConnection_uuid());
        params.put("project_uuid", blenderRenderTask.getProject_uuid());
        params.put("part_number", Integer.toString(blenderRenderTask.getBlenderFramePart().getPartNumber()));
        params.put("frame_number", Integer.toString(blenderRenderTask.getBlenderFramePart().getFrameNumber()));
        params.put("render_time", Long.toString(blenderRenderTask.getRenderTime()));
        String renderedFileName = String.format("%04d", blenderRenderTask.getBlenderFramePart().getFrameNumber());

        File result = new File(blenderRenderTask.getRenderDir() + File.separator + renderedFileName + "." + blenderRenderTask.getBlenderFramePart().getFileExtension());
        return sethlansAPIConnectionService.uploadToRemotePOST(serverUrl, params, result);
    }

    private boolean downloadRequiredFiles(File renderDir, BlenderRenderTask renderTask) {
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();
        String cachedBlenderBinaries = SethlansUtils.getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES.toString());

        if (renderDir.mkdirs()) {
            //Download Blender from server
            String[] cachedBinariesList;
            boolean versionCached = false;
            cachedBinariesList = cachedBlenderBinaries.split(",");
            for (String binary : cachedBinariesList) {
                if (binary.equals(renderTask.getBlenderVersion())) {
                    versionCached = true;
                    LOG.debug(binary + " renderer is already cached.  Skipping Download.");
                }
            }

            if (!versionCached) {
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
                String params = "connection_uuid=" + renderTask.getConnection_uuid() + "&version=" +
                        renderTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
                String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);


                if (SethlansUtils.archiveExtract(filename, new File(binDir))) {
                    LOG.debug("Extraction complete.");
                    LOG.debug("Attempting to rename blender directory. Will attempt 3 tries.");
                    int count = 0;
                    while (!SethlansUtils.renameBlenderDir(renderDir, new File(binDir), renderTask, cachedBlenderBinaries)) {
                        count++;
                        if (count == 2) {
                            return false;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                renderTask.setRenderDir(renderDir.toString());
                renderTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(new File(binDir), renderTask.getBlenderVersion()));
            }

            // Download Blend File from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blend_file/";
            String params = "connection_uuid=" + renderTask.getConnection_uuid() + "&project_uuid=" + renderTask.getProject_uuid();
            String blendFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, renderDir.toString());
            renderTask.setBlendFilename(blendFile);
            LOG.debug("Required files downloaded.");
            return true;
        }
        return false;
    }

    private Long executeRenderTask(BlenderRenderTask renderTask, String blenderScript) {
        String error;
        BlenderFramePart blenderFramePart = renderTask.getBlenderFramePart();
        try {
            LOG.debug("Starting the render of " + renderTask.getProjectName() + " Frame " + blenderFramePart.getFrameNumber() + ": Part: " + blenderFramePart.getPartNumber());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(renderTask.getBlenderExecutable());

            commandLine.addArgument("-b");
            commandLine.addArgument(renderTask.getRenderDir() + File.separator + renderTask.getBlendFilename());
            commandLine.addArgument("-P");
            commandLine.addArgument(blenderScript);
            commandLine.addArgument("-E");
            commandLine.addArgument("CYCLES");
            commandLine.addArgument("-o");
            commandLine.addArgument(renderTask.getRenderDir() + File.separator);
            commandLine.addArgument("-f");
            commandLine.addArgument(Integer.toString(renderTask.getBlenderFramePart().getFrameNumber()));
            if (renderTask.getComputeType().equals(ComputeType.CPU)) {
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

            LOG.debug("Render Output:");
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


            LOG.debug("Error Output:");
            while ((error = errorIn.readLine()) != null) {
                LOG.debug(error);
            }

            String[] timeToConvert;
            if (time != null) {
                timeToConvert = time.split(":");
                int minutes = Integer.parseInt(timeToConvert[0]);
                int seconds = Integer.parseInt(timeToConvert[1]);
                int timeInSeconds = seconds + 60 * minutes;
                long timeInMilliseconds = TimeUnit.MILLISECONDS.convert(timeInSeconds, TimeUnit.SECONDS);
                LOG.debug("Benchmark time in milliseconds: " + timeInMilliseconds);
                return timeInMilliseconds;
            }


        } catch (IOException | NullPointerException | InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        }
        return -1L;

    }

    private List<String> getUnselectedIds(List<String> cudaList) {
        List<GPUDevice> gpuDeviceList = GPU.listDevices();
        List<String> unselectedGPUs = new ArrayList<>();
        for (GPUDevice gpuDevice : gpuDeviceList) {
            for (String cuda : cudaList) {
                if (!cuda.equals(gpuDevice.getDeviceID())) {
                    unselectedGPUs.add(gpuDevice.getDeviceID());
                }
            }
        }
        List<String> unselectedIds = new ArrayList<>();
        for (String gpUs : unselectedGPUs) {
            unselectedIds.add(StringUtils.substringAfter(gpUs, "_"));
        }
        return unselectedIds;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderRenderTaskDatabaseService(BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService) {
        this.blenderRenderTaskDatabaseService = blenderRenderTaskDatabaseService;
    }

    @Autowired
    public void setBlenderPythonScriptService(BlenderPythonScriptService blenderPythonScriptService) {
        this.blenderPythonScriptService = blenderPythonScriptService;
    }
}
