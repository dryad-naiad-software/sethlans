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

import com.dryadandnaiad.sethlans.blender.BlenderScript;
import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.testutils.TestFileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.*;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 5/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class MakeParts {

    static File BIN_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "test-binaries");
    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();
    }

    @AfterEach
    void tearDown() {
        //FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @BeforeAll
    static void beforeAll() {
        BIN_DIRECTORY.mkdirs();
        var version = "2.82a";
        var os = QueryUtils.getOS();
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                BIN_DIRECTORY.toString(),
                os);
        BlenderUtils.extractBlender(BIN_DIRECTORY.toString(),
                os, download.toString(), version);
    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(BIN_DIRECTORY);
    }

    @Test
    void createParts() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.GPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.setUseParts(true);
        var frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartNumber(1);
        frameInfo.setPartMinX(0.0);
        frameInfo.setPartMaxX(0.3333333333333333);
        frameInfo.setPartMinY(0.6666666666666667);
        frameInfo.setPartMaxY(1.0);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.3333333333333333);
        frameInfo.setPartMaxX(0.6666666666666666);
        frameInfo.setPartMinY(0.6666666666666667);
        frameInfo.setPartMaxY(1.0);
        frameInfo.setPartNumber(2);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.6666666666666666);
        frameInfo.setPartMaxX(1.0);
        frameInfo.setPartMinY(0.6666666666666667);
        frameInfo.setPartMaxY(1.0);
        frameInfo.setPartNumber(3);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.0);
        frameInfo.setPartMaxX(0.3333333333333333);
        frameInfo.setPartMinY(0.33333333333333337);
        frameInfo.setPartMaxY(0.6666666666666667);
        frameInfo.setPartNumber(4);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.3333333333333333);
        frameInfo.setPartMaxX(0.6666666666666666);
        frameInfo.setPartMinY(0.33333333333333337);
        frameInfo.setPartMaxY(0.6666666666666667);
        frameInfo.setPartNumber(5);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.6666666666666666);
        frameInfo.setPartMaxX(1.0);
        frameInfo.setPartMinY(0.33333333333333337);
        frameInfo.setPartMaxY(0.6666666666666667);
        frameInfo.setPartNumber(6);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.0);
        frameInfo.setPartMaxX(0.3333333333333333);
        frameInfo.setPartMinY(0.0);
        frameInfo.setPartMaxY(0.33333333333333337);
        frameInfo.setPartNumber(7);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.3333333333333333);
        frameInfo.setPartMaxX(0.6666666666666666);
        frameInfo.setPartMinY(0.0);
        frameInfo.setPartMaxY(0.33333333333333337);
        frameInfo.setPartNumber(8);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        frameInfo = renderTask.getFrameInfo();
        frameInfo.setPartMinX(0.6666666666666666);
        frameInfo.setPartMaxX(1.0);
        frameInfo.setPartMinY(0.0);
        frameInfo.setPartMaxY(0.33333333333333337);
        frameInfo.setPartNumber(9);
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();


    }

    private RenderTask makeRenderTask(String version, String blendFile,
                                      ComputeOn computeOn, BlenderEngine engine, String blenderExecutable) {
        var taskDir = new File(TEST_DIRECTORY + File.separator + "render");
        var tileSize = 256;
        taskDir.mkdirs();
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CUDA_0");
        deviceIDs.add("CUDA_1");
        deviceIDs.add("CUDA_2");

        var scriptInfo = TaskScriptInfo.builder()
                .blenderEngine(engine)
                .computeOn(computeOn)
                .deviceIDs(deviceIDs)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(100)
                .taskTileSize(tileSize)
                .samples(50)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        var frameInfo = TaskFrameInfo.builder()
                .frameNumber(1)
                .build();
        return RenderTask.builder()
                .projectID("4d6e2507-652d-4c82-aab2-64708f833da4")
                .taskID(UUID.randomUUID().toString())
                .taskDir(taskDir.toString())
                .blenderVersion(version)
                .blenderExecutable(blenderExecutable)
                .scriptInfo(scriptInfo)
                .frameInfo(frameInfo)
                .isBenchmark(false)
                .taskBlendFile(blendFile)
                .projectName("A Sample Project")
                .build();
    }
}
