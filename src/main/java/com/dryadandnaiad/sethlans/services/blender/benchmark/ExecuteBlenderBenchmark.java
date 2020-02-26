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

package com.dryadandnaiad.sethlans.services.blender.benchmark;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
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
 * Created Mario Estrella on 9/1/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class ExecuteBlenderBenchmark {
    private static final Logger LOG = LoggerFactory.getLogger(ExecuteBlenderBenchmark.class);

    static int executeBlenderBenchmark(BlenderBenchmarkTask benchmarkTask, String blenderScript) {
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
                commandLine.addArgument(SethlansConfigUtils.getProperty(SethlansConfigKeys.CPU_CORES));
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
                    time = SethlansQueryUtils.getRenderTime(output, time);
                }
            }
            in.close();
            BufferedReader errorIn = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(errorStream.toByteArray())));


            LOG.debug("Error Output");
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
                int timeInMilliseconds = (int) TimeUnit.MILLISECONDS.convert(timeInSeconds, TimeUnit.SECONDS);
                LOG.debug("Benchmark time in milliseconds: " + timeInMilliseconds);
                return timeInMilliseconds;
            }


        } catch (IOException | NullPointerException | InterruptedException e) {
            LOG.error(Throwables.getStackTraceAsString(e));

        }
        return -1;
    }
}
