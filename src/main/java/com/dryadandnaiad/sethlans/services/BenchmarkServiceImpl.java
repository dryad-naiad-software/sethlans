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
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

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

    private final RenderTaskRepository renderTaskRepository;

    public BenchmarkServiceImpl(RenderTaskRepository renderTaskRepository) {
        this.renderTaskRepository = renderTaskRepository;
    }

    @Override
    @Async
    public void processBenchmarkRequest(Server server, BlenderArchive blenderArchive) {
        var benchmarkDir = ConfigUtils.getProperty(ConfigKeys.BENCHMARK_DIR);
        var blenderExecutableList = PropertiesUtils.getInstalledBlenderExecutables();
        if (blenderExecutableList.isEmpty()) {
            BlenderUtils.installBlenderFromServer(blenderArchive,
                    server, ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID));
        }
        var blenderExecutable = BlenderUtils.getLatestExecutable();
        var blenderVersion = blenderExecutable.getBlenderVersion();
        var benchmarkBlend = new File(benchmarkDir + File.separator + "bmw27.blend");
        if (!benchmarkBlend.exists()) {
            BlenderUtils.copyBenchmarkToDisk(benchmarkDir);
        }
        var nodeType = PropertiesUtils.getNodeType();
        createBenchmarkTasks(nodeType, blenderExecutable.toString(),
                benchmarkBlend.toString(), blenderVersion, server.getSystemID());
        var benchmarksToExecute =
                renderTaskRepository.findRenderTasksByBenchmarkIsTrueAndInProgressIsFalseAndCompleteIsFalse();
        for (RenderTask renderTask : benchmarksToExecute) {
            renderTask.setInProgress(true);
            var renderTime = BlenderUtils.executeRenderTask(renderTask, false);
            if (renderTime != null) {
                renderTask.setRenderTime(renderTime);
                renderTask.setComplete(true);
                renderTask.setInProgress(false);
                try {
                    if (renderTask.getScriptInfo().getDeviceIDs().get(0).contains("CPU"))
                        ConfigUtils.writeProperty(ConfigKeys.CPU_RATING, String.valueOf(renderTime));
                    else {
                        var selectedGPUs = PropertiesUtils.getSelectedGPUs();
                        for (GPU gpUs : selectedGPUs) {
                            if (renderTask.getScriptInfo().getDeviceIDs().get(0).equals(gpUs.getGpuID())) {
                                gpUs.setRating(renderTime);
                            }
                        }
                        PropertiesUtils.updateSelectedGPUs(selectedGPUs);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
            renderTaskRepository.save(renderTask);
        }


    }

    @Override
    public boolean benchmarkStatus(Server server) {
        return false;
    }

    private void createBenchmarkTasks(NodeType nodeType, String blenderExecutable,
                                      String benchmarkFile, String blenderVersion, String serverSystemID) {
        log.info("Creating Benchmarks");
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
        var taskServerInfo = TaskServerInfo.builder()
                .systemID(serverSystemID)
                .serverQueueID("N/A").build();
        var selectedGPUs = PropertiesUtils.getSelectedGPUs();

        switch (nodeType) {
            case CPU:
                benchmarks.add(cpuBenchmark(taskScriptInfo, blenderExecutable,
                        taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                break;
            case GPU:
                for (GPU gpu : selectedGPUs) {
                    benchmarks.add(gpuBenchmark(gpu, taskScriptInfo, blenderExecutable,
                            taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                }
                break;
            case CPU_GPU:
                benchmarks.add(cpuBenchmark(taskScriptInfo, blenderExecutable,
                        taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                for (GPU gpu : selectedGPUs) {
                    benchmarks.add(gpuBenchmark(gpu, taskScriptInfo, blenderExecutable,
                            taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                }
        }
        for (RenderTask benchmark : benchmarks) {
            renderTaskRepository.save(benchmark);
        }
    }

    private RenderTask cpuBenchmark(TaskScriptInfo taskScriptInfo, String blenderExecutable,
                                    TaskFrameInfo taskFrameInfo,
                                    TaskServerInfo taskServerInfo, String benchmarkFile, String blenderVersion) {
        taskScriptInfo.setComputeOn(ComputeOn.CPU);
        taskScriptInfo.setCores(PropertiesUtils.getSelectedCores());
        taskScriptInfo.setDeviceType(DeviceType.CPU);
        return RenderTask.builder()
                .blenderExecutable(blenderExecutable)
                .frameInfo(taskFrameInfo)
                .scriptInfo(taskScriptInfo)
                .serverInfo(taskServerInfo)
                .taskBlendFile(benchmarkFile)
                .benchmark(true)
                .projectName("CPU Benchmark " + UUID.randomUUID().toString())
                .blenderVersion(blenderVersion)
                .taskID(UUID.randomUUID().toString())
                .taskDir(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR))
                .build();
    }

    private RenderTask gpuBenchmark(GPU gpu, TaskScriptInfo taskScriptInfo, String blenderExecutable,
                                    TaskFrameInfo taskFrameInfo,
                                    TaskServerInfo taskServerInfo, String benchmarkFile, String blenderVersion) {
        taskScriptInfo.setComputeOn(ComputeOn.GPU);
        var deviceIDList = new ArrayList<String>();
        deviceIDList.add(gpu.getGpuID());
        taskScriptInfo.setDeviceType(gpu.getDeviceType());
        taskScriptInfo.setDeviceIDs(deviceIDList);
        return RenderTask.builder()
                .blenderExecutable(blenderExecutable)
                .frameInfo(taskFrameInfo)
                .scriptInfo(taskScriptInfo)
                .serverInfo(taskServerInfo)
                .taskBlendFile(benchmarkFile)
                .benchmark(true)
                .projectName(gpu.getGpuID() + " Benchmark " + UUID.randomUUID().toString())
                .blenderVersion(blenderVersion)
                .taskID(UUID.randomUUID().toString())
                .taskDir(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR))
                .build();

    }

}
