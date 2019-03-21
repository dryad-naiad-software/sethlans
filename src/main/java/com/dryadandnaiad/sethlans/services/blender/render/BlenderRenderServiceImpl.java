/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.blender.render;

import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTaskHistory;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.blender.BlenderPythonScriptService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskHistoryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.google.common.base.Throwables;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dryadandnaiad.sethlans.services.blender.render.DownloadProjectFiles.downloadRequiredFiles;
import static com.dryadandnaiad.sethlans.services.blender.render.ExecuteRenderTask.executeRenderTask;
import static com.dryadandnaiad.sethlans.services.blender.render.PrepareRenderScripts.*;
import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

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
        if (downloadRequiredFiles(sethlansServerDatabaseService, sethlansAPIConnectionService, binDir, renderDir, blendFileDir, renderTask)) {
            renderTask = renderTaskDatabaseService.saveOrUpdate(renderTask);
            String script;
            switch (renderTask.getBlenderEngine()) {
                case BLENDER_RENDER:
                    script = setBlenderRenderScript(blenderPythonScriptService, renderTaskHistoryDatabaseService, tileSizeCPU, renderTask);
                    saveOnSuccess(renderTask, script);
                    break;
                case CYCLES:
                    if (renderTask.getComputeType().equals(ComputeType.GPU)) {
                        script = setCyclesGPURenderScript(blenderPythonScriptService, renderTaskHistoryDatabaseService, tileSizeGPU, renderTask, nodeInfo);
                        saveOnSuccess(renderTask, script);
                        break;

                    } else {
                        LOG.info("Running render task using CPU");
                        script = setCyclesCPURenderScript(blenderPythonScriptService, renderTaskHistoryDatabaseService, tileSizeCPU, renderTask);
                        saveOnSuccess(renderTask, script);
                        break;
                    }
            }
        }
    }

    private void saveOnSuccess(RenderTask renderTask, String script) {
        Long renderTime = executeRenderTask(cores, renderTask, script, renderTaskDatabaseService);
        String renderedFileName = String.format("%04d", renderTask.getBlenderFramePart().getFrameNumber());
        File result = new File(renderTask.getRenderDir() + File.separator + renderedFileName + "." + renderTask.getBlenderFramePart().getFileExtension());
        renderTask = renderTaskDatabaseService.getById(renderTask.getId());
        if (renderTime != -1L && result.exists()) {
            LOG.info("Render Successful! Updating task status.");
            renderTask.setInProgress(false);
            renderTask.setComplete(true);
            renderTask.setRenderTime(renderTime);
            renderTask = renderTaskDatabaseService.saveOrUpdate(renderTask);
            int count = 0;
            while (true) {
                if (sendResultsToServer(renderTask.getConnectionUUID(), renderTask)) {
                    RenderTaskHistory renderTaskHistory = renderTaskHistoryDatabaseService.findByQueueUUID(renderTask.getRenderTaskUUID());
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
            RenderTaskHistory renderTaskHistory = renderTaskHistoryDatabaseService.findByQueueUUID(renderTask.getRenderTaskUUID());
            renderTaskHistory.setCompleted(true);
            renderTaskHistory.setFailed(true);
            renderTaskHistoryDatabaseService.saveOrUpdate(renderTaskHistory);
            SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnectionUUID());
            String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_reject_item/";
            String params = "queue_item_uuid=" + renderTask.getServerQueueUUID();
            sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
            try {
                LOG.debug("Cleaning up " + renderTask.getRenderDir());
                Thread.sleep(2000);
                FileUtils.deleteDirectory(new File(renderTask.getRenderDir()));
                renderTaskDatabaseService.delete(renderTask);
            } catch (IOException | InterruptedException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
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
