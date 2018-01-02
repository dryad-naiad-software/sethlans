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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
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

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;

    @Override
    @Async
    public void startRenderTask(String projectUUID) {
        BlenderRenderTask blenderRenderTask = blenderRenderTaskDatabaseService.getByProjectUUID(projectUUID);
        File renderDir = new File(cacheDir + File.separator + blenderRenderTask.getBlenderFramePart().getPartFilename());
        if (downloadRequiredFiles(renderDir, blenderRenderTask)) {

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
            String params = "?connection_uuid=" + renderTask.getConnection_uuid() + "&version=" +
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

    @Override
    public int executeBenchmarkTask(BlenderBenchmarkTask benchmarkTask, String blenderScript) {
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
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    public void setBlenderRenderTaskDatabaseService(BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService) {
        this.blenderRenderTaskDatabaseService = blenderRenderTaskDatabaseService;
    }
}
