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

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import com.google.common.base.Throwables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Created Mario Estrella on 9/16/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class ExecuteRenderTask {
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteRenderTask.class);

    static Long executeRenderTask(String cores, RenderTask renderTask, String blenderScript) {
        String error;
        BlenderFramePart blenderFramePart = renderTask.getBlenderFramePart();
        try {
            LOG.info("Starting the render of " + renderTask.getProjectName() + " Frame " + blenderFramePart.getFrameNumber() + ": Part: " + blenderFramePart.getPartNumber());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(renderTask.getBlenderExecutable());
            if (Boolean.parseBoolean(SethlansConfigUtils.getProperty(SethlansConfigKeys.BLENDER_DEBUG))) {
                commandLine.addArgument("-d");
            }

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
            //executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
            executor.execute(commandLine, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output;

            String time = null;

            while ((output = in.readLine()) != null) {
                LOG.debug("Render Output: " + output);
                switch (renderTask.getBlenderEngine()) {
                    case CYCLES:
                        if (output.toLowerCase().contains("failed to read blend file")) {
                            LOG.error("Render failed: Error: Failed to read blend file ... Missing DNA block");
                            return -1L;
                        }
                        if (output.toLowerCase().contains("saving:")) {
                            time = SethlansQueryUtils.getRenderTime(output, time);
                        }
                        break;
                    case BLENDER_RENDER:
                        if (output.toLowerCase().contains("saving:")) {
                            time = SethlansQueryUtils.getRenderTime(output, time);
                        }
                }

            }
            in.close();

            BufferedReader errorIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(errorStream.toByteArray())));

            while ((error = errorIn.readLine()) != null) {
                LOG.debug("Error Output: " + error);
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
}
