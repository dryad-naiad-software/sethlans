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
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
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

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;

    @Override
    @Async
    public void resumeRenderOnNodeRestart() {
        // If a node gets shutdown, this will attempt to process any pending benchmarks.
        try {
            Thread.sleep(10000);
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
            } else if (pendingRenderTask.size() == 1) {
                LOG.debug("There is one render task pending.");
                startRenderTask(pendingRenderTask.get(0).getProject_uuid());
            } else {
                LOG.debug("No render tasks are pending.");
            }
        } catch (InterruptedException e) {
            LOG.debug("Shutting down Render Service");
        }
    }

    @Override
    @Async
    public void startRenderTask(String projectUUID) {
        BlenderRenderTask blenderRenderTask = blenderRenderTaskDatabaseService.getByProjectUUID(projectUUID);
        File renderDir = new File(cacheDir + File.separator + blenderRenderTask.getBlenderFramePart().getPartFilename());
        if (downloadRequiredFiles(renderDir, blenderRenderTask)) {
            blenderRenderTask = blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);



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
                    renderTask.getBlenderFramePart().setRenderDir(renderDir.toString());
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

    public void executeRenderTask(BlenderRenderTask renderTask, String blenderScript) {
        String error = null;
        BlenderFramePart blenderFramePart = renderTask.getBlenderFramePart();
        try {
            LOG.debug("Starting the render of " + renderTask.getProjectName() + ": Part: " + blenderFramePart.getPartNumber());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(renderTask.getBlenderExecutable());

        } catch (NullPointerException e) {

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
    public void setBlenderRenderTaskDatabaseService(BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService) {
        this.blenderRenderTaskDatabaseService = blenderRenderTaskDatabaseService;
    }
}
