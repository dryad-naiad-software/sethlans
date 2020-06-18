/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * File created by Mario Estrella on 6/17/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Slf4j
@Profile({"NODE", "DUAL"})
public class BenchmarkServiceImpl implements BenchmarkService {

    @Override
    @Async
    public void processBenchmarkRequest(Server server) {
        var benchmarkDir = ConfigUtils.getProperty(ConfigKeys.BENCHMARK_DIR);
        var blenderExectuable = BlenderUtils.latestBlenderCheck(server);
        var benchmarkBlend = new File(benchmarkDir + File.separator + "bmw27.blend");
        if (!benchmarkBlend.exists()) {
            BlenderUtils.copyBenchmarkToDisk(benchmarkDir);
        }
        var nodeType = PropertiesUtils.getNodeType();


    }

    @Override
    public boolean benchmarkStatus(Server server) {
        return false;
    }

    private List<RenderTask> benchmarks(NodeType nodeType, String blenderExectuable) {
        var benchmarks = new ArrayList<RenderTask>();
        var taskFrameInfo = TaskFrameInfo.builder().frameNumber(1).build();
        var taskScriptInfo = TaskScriptInfo.builder()
                .taskResolutionX(800)
                .taskResolutionY(600)
                .taskResPercentage(50)
                .samples(50)
                .blenderEngine(BlenderEngine.CYCLES)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();

        return benchmarks;
    }

}
