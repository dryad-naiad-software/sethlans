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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.google.common.base.Throwables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

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

    public void executeRenderTask(BlenderRenderTask renderTask, String blenderScript) {
        String error = null;
//        try {
//            LOG.debug("Starting the render of " + renderTask.getProjectName() + ": Part: " + renderTask.getPart());
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
//            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
//            CommandLine commandLine = new CommandLine(renderTask.getBlenderExecutable());
//
//        } catch (IOException | NullPointerException e){
//
//        }

    }

    @Override
    public void executeBenchmarkTask(BlenderBenchmarkTask benchmarkTask, String blenderScript) {
        String error = null;
        try {
            LOG.debug("Starting Benchmark. Benchmark type: " + benchmarkTask.getComputeType());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(benchmarkTask.getBlenderExecutable());

            commandLine.addArgument("-b");
            commandLine.addArgument(benchmarkTask.getBenchmarkDir() + File.separator + benchmarkTask.getBenchmarkFile());
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
            commandLine.addArgument("-P");
            commandLine.addArgument(blenderScript);
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
            }

            error = errorStream.toString();

            LOG.debug(error);

        } catch (IOException | NullPointerException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
