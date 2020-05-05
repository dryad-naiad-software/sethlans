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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 5/3/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class BlenderScriptsTest {

    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    void writeRenderScriptBenchmark() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CPU");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .cores(6)
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.CPU)
                .deviceIDs(deviceIDs)
                .blenderVersion("2.82a")
                .isBenchmark(true)
                .useParts(false)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
    }

    @Test
    void writeRenderScriptEEVEE() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("OPENCL_0");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.BLENDER_EEVEE)
                .computeOn(ComputeOn.GPU)
                .blenderVersion("2.82a")
                .deviceIDs(deviceIDs)
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        renderTask.setBlenderVersion("2.79b");
        renderTask.setTaskID(UUID.randomUUID().toString());
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isFalse();
    }

    @Test
    void writeRenderScriptBlenderRender() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("OPENCL_0");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.BLENDER_RENDER)
                .computeOn(ComputeOn.CPU)
                .blenderVersion("2.79b")
                .deviceIDs(deviceIDs)
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        renderTask.setBlenderVersion("2.82a");
        renderTask.setTaskID(UUID.randomUUID().toString());
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isFalse();
    }


    @Test
    void writeRenderScriptMultiOpenCL() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("OPENCL_0");
        deviceIDs.add("OPENCL_1");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.GPU)
                .deviceIDs(deviceIDs)
                .blenderVersion("2.79b")
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
    }

    @Test
    void writeRenderScriptOptix() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("OPTIX_0");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.GPU)
                .deviceIDs(deviceIDs)
                .blenderVersion("2.82a")
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
    }

    @Test
    void writeRenderScriptOptixLegacy() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("OPTIX_0");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.GPU)
                .deviceIDs(deviceIDs)
                .blenderVersion("2.79b")
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
    }

    @Test
    void writeRenderScriptMultiCuda() {
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CUDA_0");
        deviceIDs.add("CUDA_1");
        var renderTask = RenderTask.builder()
                .taskID(UUID.randomUUID().toString())
                .taskDir(TEST_DIRECTORY.toString())
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.GPU)
                .deviceIDs(deviceIDs)
                .blenderVersion("2.79b")
                .isBenchmark(false)
                .useParts(true)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(50)
                .taskTileSize(256)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
    }
}
