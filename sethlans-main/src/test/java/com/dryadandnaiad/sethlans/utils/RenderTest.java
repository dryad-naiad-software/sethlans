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
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.testutils.TestFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
    void executeRenderTaskEeveeFrame() {
        var file1 = "wasp_bot.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.82a";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_EEVEE, blenderExecutable);
        renderTask.getScriptInfo().setTaskResPercentage(100);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskEeveeFramePart() {
        var file1 = "wasp_bot.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.82a";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_EEVEE, blenderExecutable);
        renderTask.getScriptInfo().setTaskResPercentage(100);
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
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + "-part-" +
                renderTask.getFrameInfo().getPartNumber() + ".png");
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskCyclesCPUFrame27x() {
        var file1 = "bmw27_gpu.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.79b";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskBlenderRenderFrame() {
        var file1 = "refract_monkey.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.79b";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.BLENDER_RENDER, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskCyclesCPUFrame() {
        var file1 = "bmw27_gpu.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.82a";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.CPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setCores(4);
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + ".png");
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        assertThat(finalFile).exists();
    }

    @Test
    void executeRenderTaskCyclesCPUFramePart() {
        var file1 = "bmw27_gpu.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.82a";
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
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
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNull();
        assertThat(result).isNotNegative();
        assertThat(result).isGreaterThan(0L);
        var finalFile = new File(renderTask.getTaskDir() + File.separator +
                QueryUtils.truncatedProjectNameAndID(renderTask.getProjectName(), renderTask.getProjectID())
                + "-000" + renderTask.getFrameInfo().getFrameNumber() + "-part-" +
                renderTask.getFrameInfo().getPartNumber() + ".png");
        assertThat(finalFile).exists();
    }

    @Disabled
    @Test
    void executeRenderTaskCyclesGPU() {
        var file1 = "bmw27_gpu.blend";
        var os = QueryUtils.getOS();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "blend_files/" + file1, file1);
        var binaryDir = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDir.mkdirs();
        var version = "2.82a";
        var tileSize = 256;
        var deviceIDs = new ArrayList<String>();
        deviceIDs.add("CUDA_0");
        deviceIDs.add("CUDA_1");
        var download = BlenderUtils.downloadBlenderToServer(version,
                "resource",
                TEST_DIRECTORY.toString(),
                os);
        assertThat(BlenderUtils.extractBlender(binaryDir.toString(),
                os, download.toString(), version)).isTrue();
        var blenderExecutable = BlenderUtils.getBlenderExecutable(binaryDir.toString(), version);
        var renderTask = makeRenderTask(version, TEST_DIRECTORY + File.separator + file1,
                ComputeOn.GPU, BlenderEngine.CYCLES, blenderExecutable);
        renderTask.getScriptInfo().setDeviceIDs(deviceIDs);
        renderTask.getScriptInfo().setTaskTileSize(tileSize);
        assertThat(BlenderScripts.writeRenderScript(renderTask)).isTrue();
        var result = BlenderUtils.executeRenderTask(renderTask, true);
        log.info("Task completed in " + QueryUtils.getTimeFromMills(result));
        assertThat(result).isNotNull();
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
