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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Value("${sethlans.cuda}")
    private String cuda;

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
    public void resumeRenderOnNodeRestart() {
        // If a node gets shutdown, this will attempt to process any pending benchmarks.
        try {
            Thread.sleep(14000);
            LOG.debug("Checking to see if any render tasks are pending.");
            List<BlenderRenderTask> blenderRenderTaskList = blenderRenderTaskDatabaseService.listAll();
            List<BlenderRenderTask> pendingRenderTask = new ArrayList<>();
            for (BlenderRenderTask blenderRenderTask : blenderRenderTaskList) {
                if (!blenderRenderTask.isComplete()) {
                    pendingRenderTask.add(blenderRenderTask);
                }
            }

            if (pendingRenderTask.size() > 1) {
                LOG.debug("There are " + pendingRenderTask.size() + " render tasks pending.");
                List<String> projectUUIDs = new ArrayList<>();
                for (BlenderRenderTask blenderRenderTask : pendingRenderTask) {
                    projectUUIDs.add(blenderRenderTask.getProject_uuid());
                }
                // TODO create a queue if there are than one tasks pending(shouldn't happen.)
            } else if (pendingRenderTask.size() == 1) {
                LOG.debug("There is one render task pending.");
                startRenderService(pendingRenderTask.get(0).getProject_uuid());
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
                List<String> cudaList = Arrays.asList(cuda.split(","));
                List<String> cudaIDList = new ArrayList<>();
                LOG.debug("Running render task using " + cuda);
                for (String cuda : cudaList) {
                    cudaIDList.add(StringUtils.substringAfter(cuda, "_"));
                }
                String script = blenderPythonScriptService.writeRenderPythonScript(blenderRenderTask.getComputeType(),
                        blenderRenderTask.getRenderDir(), cudaIDList,
                        getUnselectedIds(cudaList),
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
                        emptyList, tileSizeCPU,
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
        if (executeRenderTask(blenderRenderTask, script)) {
            LOG.debug("Render Successful! Updating task status.");
            blenderRenderTask.setInProgress(false);
            blenderRenderTask.setComplete(true);
            blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);
        }
    }

    private boolean downloadRequiredFiles(File renderDir, BlenderRenderTask renderTask) {
        LOG.debug("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnection_uuid());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();

        if (renderDir.mkdirs()) {
            //Download Blender from server
            String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
            String params = "connection_uuid=" + renderTask.getConnection_uuid() + "&version=" +
                    renderTask.getBlenderVersion() + "&os=" + SethlansUtils.getOS();
            String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, renderDir.toString());

            if (SethlansUtils.archiveExtract(filename, renderDir)) {
                LOG.debug("Extraction complete.");
                if (SethlansUtils.renameBlender(renderDir, renderTask.getBlenderVersion())) {
                    LOG.debug("Blender executable ready");
                    renderTask.setRenderDir(renderDir.toString());
                    renderTask.setBlenderExecutable(SethlansUtils.assignBlenderExecutable(renderDir));
                } else {
                    LOG.debug("Rename failed.");
                    return false;
                }
            }

            // Download Blend File from server
            connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blend_file/";
            params = "connection_uuid=" + renderTask.getConnection_uuid() + "&project_uuid=" + renderTask.getProject_uuid();
            String blendFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, renderDir.toString());
            renderTask.setBlendFilename(blendFile);
            LOG.debug("Required files downloaded.");
            return true;


        }
        return false;
    }

    private boolean executeRenderTask(BlenderRenderTask renderTask, String blenderScript) {
        boolean success = false;
        String error;
        BlenderFramePart blenderFramePart = renderTask.getBlenderFramePart();
        try {
            LOG.debug("Starting the render of " + renderTask.getProjectName() + ": Part: " + blenderFramePart.getPartNumber());
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

            while ((output = in.readLine()) != null) {
                LOG.debug(output);
                if (output.contains("Finished")) {
                    success = true;
                }
            }


            error = errorStream.toString();

            LOG.debug(error);


        } catch (IOException | NullPointerException | InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        }
        return success;

    }

    private List<String> getUnselectedIds(List<String> cudaList) {
        List<GPUDevice> gpuDeviceList = GPU.listDevices();
        List<String> unselectedGPUs = new ArrayList<>();
        for (GPUDevice gpuDevice : gpuDeviceList) {
            for (String cuda : cudaList) {
                if (!cuda.equals(gpuDevice.getCudaName())) {
                    unselectedGPUs.add(gpuDevice.getCudaName());
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
