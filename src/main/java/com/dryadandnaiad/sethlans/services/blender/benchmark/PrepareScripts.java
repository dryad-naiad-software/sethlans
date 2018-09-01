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
import com.dryadandnaiad.sethlans.services.blender.BlenderPythonScriptService;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.dryadandnaiad.sethlans.services.blender.benchmark.DownloadBenchmarkFiles.downloadRequiredFiles;
import static com.dryadandnaiad.sethlans.services.blender.benchmark.ExecuteBlenderBenchmark.executeBlenderBenchmark;

/**
 * Created Mario Estrella on 9/1/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class PrepareScripts {
    private static final Logger LOG = LoggerFactory.getLogger(PrepareScripts.class);

    static void prepareScriptandExecute(BlenderBenchmarkTask benchmarkTask,
                                        SethlansAPIConnectionService sethlansAPIConnectionService,
                                        SethlansServerDatabaseService sethlansServerDatabaseService,
                                        BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService,
                                        BlenderPythonScriptService blenderPythonScriptService) {
        String tempDir = SethlansConfigUtils.getProperty(SethlansConfigKeys.TEMP_DIR);
        LOG.debug("Processing benchmark task: " + benchmarkTask.toString());
        File benchmarkDir = new File(tempDir + File.separator + benchmarkTask.getBenchmark_uuid() + "_" + benchmarkTask.getBenchmarkURL());
        if (downloadRequiredFiles(benchmarkDir, benchmarkTask, sethlansAPIConnectionService, sethlansServerDatabaseService)) {
            benchmarkTask = blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);

            if (benchmarkTask.getComputeType().equals(ComputeType.GPU)) {
                LOG.debug("Creating benchmark script using " + benchmarkTask.getDeviceID());
                String deviceID = StringUtils.substringAfter(benchmarkTask.getDeviceID(), "_");
                String script;
                if (SethlansQueryUtils.isCuda(benchmarkTask.getDeviceID())) {
                    LOG.debug("CUDA Device found, using cuda parameters for script");
                    script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                            benchmarkTask.getBenchmarkDir(), deviceID, true, "128", 800, 600, 50);
                } else {
                    LOG.debug("OpenCL Device found, using opencl parameters for script");

                    script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                            benchmarkTask.getBenchmarkDir(), deviceID, false, "128", 800, 600, 50);
                }

                int rating = executeBlenderBenchmark(benchmarkTask, script);
                if (rating == -1) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    LOG.debug(benchmarkTask.toString());
                    benchmarkTask.setGpuRating(rating);
                    benchmarkTask.setInProgress(false);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                }

            } else {
                LOG.debug("Creating benchmark script using CPU");
                String script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                        benchmarkTask.getBenchmarkDir(), "0", false, "16", 800, 600, 50);
                int rating = executeBlenderBenchmark(benchmarkTask, script);
                if (rating == -1) {
                    LOG.debug("Benchmark failed.");
                    LOG.debug(benchmarkTask.toString());
                } else {
                    LOG.debug("Benchmark complete, saving to database.");
                    benchmarkTask.setCpuRating(rating);
                    benchmarkTask.setInProgress(false);
                    benchmarkTask.setComplete(true);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(benchmarkTask);
                    LOG.debug(benchmarkTask.toString());
                }
            }
        }
    }
}
