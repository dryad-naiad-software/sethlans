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
import com.dryadandnaiad.sethlans.services.blender.BlenderPythonScriptService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created Mario Estrella on 9/1/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class PrepareBenchmarkScripts {
    private static final Logger LOG = LoggerFactory.getLogger(PrepareBenchmarkScripts.class);

    static String prepareGPUCyclesBenchmarkScript(BlenderPythonScriptService blenderPythonScriptService, BlenderBenchmarkTask benchmarkTask) {
        LOG.debug("Creating benchmark script using " + benchmarkTask.getDeviceID());
        String deviceID = StringUtils.substringAfter(benchmarkTask.getDeviceID(), "_");
        String script;
        if (SethlansQueryUtils.isCuda(benchmarkTask.getDeviceID())) {
            LOG.info("CUDA Device found, using cuda parameters for script");
            script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                    benchmarkTask.getBenchmarkDir(), deviceID, true, "128", 800, 600, 50);
        } else {
            LOG.info("OpenCL Device found, using opencl parameters for script");

            script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                    benchmarkTask.getBenchmarkDir(), deviceID, false, "128", 800, 600, 50);
        }
        return script;

    }

    static String prepareCPUCyclesBenchamrkScript(BlenderPythonScriptService blenderPythonScriptService, BlenderBenchmarkTask benchmarkTask) {
        LOG.info("Creating benchmark script using CPU");
        String script = blenderPythonScriptService.writeBenchmarkPythonScript(benchmarkTask.getComputeType(),
                benchmarkTask.getBenchmarkDir(), "0", false, "16", 800, 600, 50);
        return script;
    }
}
