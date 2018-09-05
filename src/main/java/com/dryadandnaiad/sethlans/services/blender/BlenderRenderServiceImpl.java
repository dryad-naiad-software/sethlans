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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTaskHistory;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskHistoryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
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

import static com.dryadandnaiad.sethlans.utils.BlenderUtils.assignBlenderExecutable;
import static com.dryadandnaiad.sethlans.utils.BlenderUtils.renameBlenderDir;
import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.archiveExtract;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.isDirectoryEmpty;

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

    @Value("${sethlans.binDir}")
    private String binDir;

    @Value("${sethlans.tileSizeCPU}")
    private String tileSizeCPU;

    @Value("${sethlans.tileSizeGPU}")
    private String tileSizeGPU;

    @Value("${sethlans.configDir}")
    private String configDir;


    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private RenderTaskDatabaseService renderTaskDatabaseService;
    private BlenderPythonScriptService blenderPythonScriptService;
    private RenderTaskHistoryDatabaseService renderTaskHistoryDatabaseService;


    @Override
    @Async
    public void clearQueueOnNodeRestart() {
        // If a node gets shutdown, this will attempt to process any pending benchmarks.
        try {
            Thread.sleep(15000);
            LOG.info("Checking to see if any render tasks are pending.");
            List<RenderTask> renderTaskList = renderTaskDatabaseService.listAll();
            if (renderTaskList.size() > 0) {
                LOG.info("Clearing all render tasks in the database");
                renderTaskDatabaseService.deleteAll();
            } else {
                LOG.debug("No render tasks are pending.");
            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting down Render Service");
        }
    }

    @Override
    @Async
    public void startRender(String queueUUID) {
        RenderTask renderTask = renderTaskDatabaseService.findByQueueUUID(queueUUID);
        renderTask.setInProgress(true);
        NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
        String cacheDir = getProperty(SethlansConfigKeys.CACHE_DIR);
        String truncatedProjectName = StringUtils.left(renderTask.getProjectName(), 10);
        String cleanedProjectName = truncatedProjectName.replaceAll(" ", "").replaceAll("[^a-zA-Z0-9_-]", "");
        File blendFileDir = new File(getProperty(SethlansConfigKeys.BLEND_FILE_CACHE_DIR)
                + File.separator + cleanedProjectName.toLowerCase() + "-" + renderTask.getProjectUUID());
        File renderDir = new File(cacheDir + File.separator + renderTask.getBlenderFramePart().getPartFilename());
        if (downloadRequiredFiles(renderDir, blendFileDir, renderTask)) {
            renderTask = renderTaskDatabaseService.saveOrUpdate(renderTask);
            String script;
            switch (renderTask.getBlenderEngine()) {
                case BLENDER_RENDER:
                    script = blenderPythonScriptService.writeBlenderRenderPythonScript(renderTask.getRenderDir(), renderTask.getRenderOutputFormat(), tileSizeCPU,
                            renderTask.getTaskResolutionX(), renderTask.getTaskResolutionY(), renderTask.getPartResPercentage(), renderTask.getBlenderFramePart().getPartPositionMaxY(),
                            renderTask.getBlenderFramePart().getPartPositionMinY());
                    saveOnSuccess(renderTask, script);
                    break;
                case CYCLES:
                    if (renderTask.getComputeType().equals(ComputeType.GPU)) {
                        script = setDeviceID(renderTask, nodeInfo);
                        saveOnSuccess(renderTask, script);
                        break;

                    } else {
                        LOG.info("Running render task using CPU");
                        List<String> emptyList = new ArrayList<>();
                        script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                                renderTask.getRenderDir(), emptyList,
                                emptyList, false, renderTask.getRenderOutputFormat(), tileSizeCPU,
                                renderTask.getTaskResolutionX(),
                                renderTask.getTaskResolutionY(),
                                renderTask.getPartResPercentage(),
                                renderTask.getSamples(),
                                renderTask.getBlenderFramePart().getPartPositionMaxY(),
                                renderTask.getBlenderFramePart().getPartPositionMinY());
                        saveOnSuccess(renderTask, script);
                        break;
                    }
            }
        }
    }

    private String setDeviceID(RenderTask renderTask, NodeInfo nodeInfo) {
        String script;
        String deviceID = getProperty(SethlansConfigKeys.GPU_DEVICE);
        List<String> deviceList = Arrays.asList(deviceID.split(","));
        List<String> deviceIDList = new ArrayList<>();
        if (nodeInfo.isCombined()) {
            boolean isCuda = false;
            LOG.info("Running render task using " + deviceID);
            for (String device : deviceList) {
                deviceIDList.add(StringUtils.substringAfter(device, "_"));
                isCuda = SethlansQueryUtils.isCuda(device);
            }
            script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                    renderTask.getRenderDir(), deviceIDList,
                    getUnselectedIds(deviceList), isCuda,
                    renderTask.getRenderOutputFormat(),
                    tileSizeGPU,
                    renderTask.getTaskResolutionX(),
                    renderTask.getTaskResolutionY(),
                    renderTask.getPartResPercentage(),
                    renderTask.getSamples(),
                    renderTask.getBlenderFramePart().getPartPositionMaxY(),
                    renderTask.getBlenderFramePart().getPartPositionMinY());
        } else {
            LOG.info("Running render task using " + renderTask.getDeviceID());
            boolean isCuda = SethlansQueryUtils.isCuda(renderTask.getDeviceID());
            deviceIDList.add(StringUtils.substringAfter(renderTask.getDeviceID(), "_"));
            script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                    renderTask.getRenderDir(), deviceIDList,
                    getUnselectedIds(deviceList), isCuda,
                    renderTask.getRenderOutputFormat(),
                    tileSizeGPU,
                    renderTask.getTaskResolutionX(),
                    renderTask.getTaskResolutionY(),
                    renderTask.getPartResPercentage(),
                    renderTask.getSamples(),
                    renderTask.getBlenderFramePart().getPartPositionMaxY(),
                    renderTask.getBlenderFramePart().getPartPositionMinY());
        }
        return script;
    }

    private void saveOnSuccess(RenderTask renderTask, String script) {
        Long renderTime = executeRenderTask(renderTask, script);
        String renderedFileName = String.format("%04d", renderTask.getBlenderFramePart().getFrameNumber());
        File result = new File(renderTask.getRenderDir() + File.separator + renderedFileName + "." + renderTask.getBlenderFramePart().getFileExtension());
        if (renderTime != -1L && result.exists()) {
            LOG.info("Render Successful! Updating task status.");
            renderTask.setInProgress(false);
            renderTask.setComplete(true);
            renderTask.setRenderTime(renderTime);
            renderTask = renderTaskDatabaseService.saveOrUpdate(renderTask);
            int count = 0;
            while (true) {
                if (sendResultsToServer(renderTask.getConnectionUUID(), renderTask)) {
                    RenderTaskHistory renderTaskHistory = renderTaskHistoryDatabaseService.findByQueueUUID(renderTask.getServerQueueUUID());
                    renderTaskHistory.setCompleted(true);
                    renderTaskHistory.setFailed(false);
                    renderTaskHistoryDatabaseService.saveOrUpdate(renderTaskHistory);
                    try {
                        LOG.debug("Cleaning up " + renderTask.getRenderDir());
                        FileUtils.deleteDirectory(new File(renderTask.getRenderDir()));
                        renderTaskDatabaseService.delete(renderTask);
                        break;
                    } catch (IOException e) {
                        LOG.error(Throwables.getStackTraceAsString(e));
                    }
                }
                if (count >= 10) {
                    LOG.error("Unable to establish a connection with the server to send results.");
                    break;
                }
                try {
                    Thread.sleep(5000);
                    count++;
                } catch (InterruptedException e) {
                    LOG.error(Throwables.getStackTraceAsString(e));
                }
            }


        } else {
            LOG.info("Failed render, sending reject notice");
            RenderTaskHistory renderTaskHistory = renderTaskHistoryDatabaseService.findByQueueUUID(renderTask.getServerQueueUUID());
            renderTaskHistory.setCompleted(true);
            renderTaskHistory.setFailed(true);
            renderTaskHistoryDatabaseService.saveOrUpdate(renderTaskHistory);
            SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnectionUUID());
            String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_reject_item/";
            String params = "queue_item_uuid=" + renderTask.getServerQueueUUID();
            sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
            renderTaskDatabaseService.delete(renderTask);
        }
    }

    private boolean sendResultsToServer(String connectionUUID, RenderTask renderTask) {
        try {
            SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connectionUUID);
            String serverUrl = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/response";
            Map<String, String> params = new HashMap<>();
            params.put("connection_uuid", renderTask.getConnectionUUID());
            params.put("queue_uuid", renderTask.getServerQueueUUID());
            params.put("project_uuid", renderTask.getProjectUUID());
            params.put("render_time", Long.toString(renderTask.getRenderTime()));
            String renderedFileName = String.format("%04d", renderTask.getBlenderFramePart().getFrameNumber());

            File result = new File(renderTask.getRenderDir() + File.separator + renderedFileName + "." + renderTask.getBlenderFramePart().getFileExtension());
            return sethlansAPIConnectionService.uploadToRemotePOST(serverUrl, params, result);
        } catch (NullPointerException e) {
            LOG.error("Server does not exist, most likely deleted.");
            return true;
        }
    }

    private boolean downloadRequiredFiles(File renderDir, File blendFileDir, RenderTask renderTask) {
        LOG.info("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnectionUUID());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();
        String cachedBlenderBinaries = getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES);

        if (renderDir.mkdirs()) {
            //Download Blender from server
            String[] cachedBinariesList;
            boolean versionCached = false;
            cachedBinariesList = cachedBlenderBinaries.split(",");
            for (String binary : cachedBinariesList) {
                if (binary.equals(renderTask.getBlenderVersion())) {
                    versionCached = true;
                    LOG.info(binary + " renderer is already cached.  Skipping Download.");
                }
            }


            if (!versionCached) {
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
                String params = "connection_uuid=" + renderTask.getConnectionUUID() + "&version=" +
                        renderTask.getBlenderVersion() + "&os=" + SethlansQueryUtils.getOS();
                String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);


                if (archiveExtract(filename, new File(binDir), true)) {
                    LOG.debug("Extraction complete.");
                    LOG.debug("Attempting to rename blender directory. Will attempt 3 tries.");
                    int count = 0;
                    while (!renameBlenderDir(renderDir, new File(binDir), renderTask, cachedBlenderBinaries)) {
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
                renderTask.setBlenderExecutable(assignBlenderExecutable(new File(binDir), renderTask.getBlenderVersion()));
            }

            // Download Blend File from server
            blendFileDir.mkdirs();
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blend_file/";
            String params = "connection_uuid=" + renderTask.getConnectionUUID() + "&project_uuid=" + renderTask.getProjectUUID();
            if (isDirectoryEmpty(blendFileDir)) {
                String blendFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, blendFileDir.toString());
                renderTask.setBlendFilename(blendFileDir + File.separator + blendFile);
                LOG.info("Required files downloaded.");
            } else {
                LOG.info("Blend file for this project exists, using cached version");
                String[] fileList = blendFileDir.list();
                if (fileList != null) {
                    String blendFile = blendFileDir + File.separator + fileList[0];
                    renderTask.setBlendFilename(blendFile);
                }
            }
            return true;
        }
        return false;
    }


    private Long executeRenderTask(RenderTask renderTask, String blenderScript) {
        String error;
        BlenderFramePart blenderFramePart = renderTask.getBlenderFramePart();
        try {
            LOG.info("Starting the render of " + renderTask.getProjectName() + " Frame " + blenderFramePart.getFrameNumber() + ": Part: " + blenderFramePart.getPartNumber());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(renderTask.getBlenderExecutable());

            commandLine.addArgument("-b");
            commandLine.addArgument(renderTask.getBlendFilename());
            commandLine.addArgument("-P");
            commandLine.addArgument(blenderScript);
            commandLine.addArgument("-E");
            switch (renderTask.getBlenderEngine()) {
                case CYCLES:
                    commandLine.addArgument("CYCLES");
                    break;
                case BLENDER_RENDER:
                    commandLine.addArgument("BLENDER_RENDER");
                    break;
            }

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
                switch (renderTask.getBlenderEngine()) {
                    case CYCLES:
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
                        break;
                    case BLENDER_RENDER:
                        if (output.contains("Saving:")) {
                            String[] finished = output.split("\\|");
                            for (String item : finished) {
                                LOG.debug(item);
                                if (item.contains(" Time:")) {
                                    time = StringUtils.substringAfter(item, ":");
                                    time = StringUtils.substringBefore(time, ".");
                                    time = time.replaceAll("\\s", "");
                                }
                            }
                        }
                }

            }
            in.close();

            BufferedReader errorIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(errorStream.toByteArray())));

            LOG.debug("Error Output:");
            while ((error = errorIn.readLine()) != null) {
                LOG.debug(error);
            }
            errorIn.close();

            String[] timeToConvert;
            if (time != null) {
                timeToConvert = time.split(":");
                int minutes = Integer.parseInt(timeToConvert[0]);
                int seconds = Integer.parseInt(timeToConvert[1]);
                int timeInSeconds = seconds + 60 * minutes;
                long timeInMilliseconds = TimeUnit.MILLISECONDS.convert(timeInSeconds, TimeUnit.SECONDS);
                LOG.info("Render time in milliseconds: " + timeInMilliseconds);
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
    public void setRenderTaskDatabaseService(RenderTaskDatabaseService renderTaskDatabaseService) {
        this.renderTaskDatabaseService = renderTaskDatabaseService;
    }

    @Autowired
    public void setBlenderPythonScriptService(BlenderPythonScriptService blenderPythonScriptService) {
        this.blenderPythonScriptService = blenderPythonScriptService;
    }

    @Autowired
    public void setRenderTaskHistoryDatabaseService(RenderTaskHistoryDatabaseService renderTaskHistoryDatabaseService) {
        this.renderTaskHistoryDatabaseService = renderTaskHistoryDatabaseService;
    }
}
