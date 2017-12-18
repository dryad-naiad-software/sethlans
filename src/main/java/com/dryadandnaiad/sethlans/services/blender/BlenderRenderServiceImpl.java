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
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            commandLine.addArgument(benchmarkTask.getBenchmarkFile());
            commandLine.addArgument("-E CYCLES");
            commandLine.addArgument("-o " + benchmarkTask.getBenchmarkDir());
            commandLine.addArgument("-f 1");
            commandLine.addArgument("-P " + blenderScript);

            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(pumpStreamHandler);
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            executor.execute(commandLine, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output = in.readLine();
            error = errorStream.toString();

        } catch (IOException | NullPointerException e) {

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
