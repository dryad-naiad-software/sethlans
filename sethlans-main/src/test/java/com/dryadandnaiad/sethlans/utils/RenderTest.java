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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 5/7/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class RenderTest {
    static File BIN_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "test-binaries");
    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");

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
        version = "2.79b";
        download = BlenderUtils.downloadBlenderToServer(version,
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

    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    @DisabledOnOs(OS.LINUX)
    void executeRenderTaskEeveeFrame() {
        var file1 = "wasp_bot.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_EEVEE, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    @DisabledOnOs(OS.LINUX)
    void executeRenderTaskEeveeFramePart() {
        var file1 = "wasp_bot.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_EEVEE, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        renderTask.setUseParts(true);
        var frameInfo = TaskFrameInfo.builder()
                .frameNumber(1)
                .partNumber(2)
                .partMinX(0.5)
                .partMaxX(1.0)
                .partMinY(0.5)
                .partMaxY(1.0).build();
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + "-" +
                renderTask.getFrameInfo().getPartNumber() + ".png");
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskCyclesCPUFrame27x() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.79b";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskBlenderRenderFrame() {
        var file1 = "refract_monkey.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.79b";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_RENDER, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskCyclesCPUFrame() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskOpenEXR() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        renderTask.getScriptInfo().setImageOutputFormat(ImageOutputFormat.OPEN_EXR);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".exr");
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskTIFF() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        renderTask.getScriptInfo().setImageOutputFormat(ImageOutputFormat.TIFF);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".tif");
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }


    @Test
    void executeRenderTaskCyclesCPUFramePart() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        renderTask.setUseParts(true);
        var frameInfo = TaskFrameInfo.builder()
                .frameNumber(1)
                .partNumber(2)
                .partMinX(0.0)
                .partMaxX(0.5)
                .partMinY(0.0)
                .partMaxY(0.5).build();
        renderTask.setFrameInfo(frameInfo);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + "-" +
                renderTask.getFrameInfo().getPartNumber() + ".png");
        assertThat(finalFile).exists();
    }

    @Disabled
    @Test
    void executeRenderTaskCyclesGPU() {
        var file1 = "bmw27_gpu.blend";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var version = "2.82a";
        var blenderExecutable = BlenderUtils.getBlenderExecutable(BIN_DIRECTORY.toString(), version);
        var tileSize = 256;
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CUDA_0");
        deviceIDs.add("CUDA_1");
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.GPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setDeviceIDs(deviceIDs);
        renderTask.getScriptInfo().setTaskTileSize(tileSize);
        assertThat(BlenderScript.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        assertThat(result).isNotNull();
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(finalFile).exists();

    }

    private RenderTask makeRenderTask(String version, String blendFile,
                                      ComputeOn computeOn, BlenderEngine engine, String blenderExecutable) {
        var taskDir = new File(TEST_DIRECTORY + File.separator + "render");
        var tileSize = 32;
        taskDir.mkdirs();
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CPU");

        var scriptInfo = TaskScriptInfo.builder()
                .blenderEngine(engine)
                .computeOn(computeOn)
                .deviceIDs(deviceIDs)
                .taskResolutionX(1920)
                .taskResolutionY(1080)
                .taskResPercentage(25)
                .taskTileSize(tileSize)
                .samples(10)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        var frameInfo = TaskFrameInfo.builder()
                .frameNumber(1)
                .build();
        return RenderTask.builder()
                .projectID(UUID.randomUUID().toString())
                .taskID(UUID.randomUUID().toString())
                .taskDir(taskDir.toString())
                .blenderVersion(version)
                .blenderExecutable(blenderExecutable)
                .scriptInfo(scriptInfo)
                .frameInfo(frameInfo)
                .isBenchmark(false)
                .taskBlendFile(blendFile)
                .projectName("A Sample Project")
                .useParts(false)
                .build();
    }
}
