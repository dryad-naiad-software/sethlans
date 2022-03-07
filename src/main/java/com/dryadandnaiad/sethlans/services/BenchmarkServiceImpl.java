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

import com.dryadandnaiad.sethlans.blender.BlenderScript;
import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
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
    private final ServerRepository serverRepository;

    public BenchmarkServiceImpl(RenderTaskRepository renderTaskRepository, ServerRepository serverRepository) {
        this.renderTaskRepository = renderTaskRepository;
        this.serverRepository = serverRepository;
    }

    @Override
    @Async
    public void processBenchmarkRequest(Server server, BlenderArchive blenderArchive) {
        log.debug("Processing Benchmark request from " + server.getHostname());
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
        createBenchmarkTasks(nodeType, blenderExecutable.getBlenderExecutable(),
                benchmarkBlend.toString(), blenderVersion, server.getSystemID());

        runBenchmarks();
        reportBenchmarkState(server, nodeType);

    }

    @Override
    @Async
    public void pendingBenchmarks() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        var benchmarksToExecute =
                renderTaskRepository.findRenderTasksByBenchmarkIsTrueAndInProgressIsFalseAndCompleteIsFalse();
        if (!benchmarksToExecute.isEmpty()) {
            log.debug("Processing pending benchmark render tasks");
            var nodeType = PropertiesUtils.getNodeType();
            var servers = new HashSet<Server>();
            for (RenderTask benchmark : benchmarksToExecute) {
                servers.add(serverRepository.findBySystemID(benchmark
                        .getServerInfo()
                        .getSystemID())
                        .orElse(null));
            }
            for (Server server : servers) {
                reportBenchmarkState(server, nodeType);
            }
        }
    }

    private void runBenchmarks() {
        var benchmarksToExecute =
                renderTaskRepository.findRenderTasksByBenchmarkIsTrueAndInProgressIsFalseAndCompleteIsFalse();
        for (RenderTask renderTask : benchmarksToExecute) {
            log.debug("Starting to execute " + renderTask.toString());
            if (BlenderScript.writeRenderScript(renderTask)) {
                renderTask.setInProgress(true);
                var renderTime = BlenderUtils.executeRenderTask(renderTask, false);
                if (renderTime != null) {
                    renderTask.setRenderTime(renderTime);
                    renderTask.setComplete(true);
                    renderTask.setInProgress(false);
                    try {
                        if (renderTask.getScriptInfo().getDeviceType().equals(DeviceType.CPU))
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
                        log.error(Throwables.getStackTraceAsString(e));
                    }
                }
                renderTaskRepository.save(renderTask);


            }
        }
    }

    private void reportBenchmarkState(Server server, NodeType nodeType) {
        if (benchmarksComplete(server)) {
            var path = "/api/v1/management/update_node_benchmark_state";
            var host = server.getIpAddress();
            var port = server.getNetworkPort();
            var node = Node.builder()
                    .nodeType(nodeType)
                    .benchmarkComplete(true)
                    .systemID(PropertiesUtils.getSystemID()).build();
            switch (nodeType) {
                case CPU:
                    node.setCpuRating(PropertiesUtils.getCPURating());
                    node.setTotalRenderingSlots(1);
                    break;
                case GPU:
                    var selectedGPUs = PropertiesUtils.getSelectedGPUs();
                    node.setSelectedGPUs(selectedGPUs);
                    if(PropertiesUtils.isGPUCombined()){
                       node.setTotalRenderingSlots(1);
                    } else {
                        node.setTotalRenderingSlots(selectedGPUs.size());
                    }
                    break;
                case CPU_GPU:
                    var selectedGPUs1 = PropertiesUtils.getSelectedGPUs();
                    node.setCpuRating(PropertiesUtils.getCPURating());
                    node.setSelectedGPUs(selectedGPUs1);
                    if(PropertiesUtils.isGPUCombined()){
                        node.setTotalRenderingSlots(2);
                    } else {
                        node.setTotalRenderingSlots(selectedGPUs1.size() + 1);
                    }
                    break;
            }
            try {
                var objectMapper = new ObjectMapper();
                var nodeAsJSON = objectMapper.writeValueAsString(node);
                NetworkUtils.postJSONToURL(path, host, port, nodeAsJSON, true);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }

        }
    }


    @Override
    public boolean benchmarksComplete(Server server) {
        var serverID = server.getSystemID();
        var benchmarkList = renderTaskRepository.findRenderTaskByBenchmarkIsTrue();
        if (benchmarkList.isEmpty()) {
            return false;
        }
        for (RenderTask benchmark : benchmarkList) {
            if (benchmark.getServerInfo().getSystemID().equals(serverID)) {
                if (!benchmark.isComplete()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void createBenchmarkTasks(NodeType nodeType, String blenderExecutable,
                                      String benchmarkFile, String blenderVersion, String serverSystemID) {
        log.info("Creating Benchmarks");
        var benchmarks = new ArrayList<RenderTask>();
        var taskFrameInfo = TaskFrameInfo.builder().frameNumber(1).build();

        var taskServerInfo = TaskServerInfo.builder()
                .systemID(serverSystemID).build();
        var selectedGPUs = PropertiesUtils.getSelectedGPUs();

        switch (nodeType) {
            case CPU:
                benchmarks.add(cpuBenchmark(blenderExecutable,
                        taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                break;
            case GPU:
                for (GPU gpu : selectedGPUs) {
                    benchmarks.add(gpuBenchmark(gpu, blenderExecutable,
                            taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion));
                }
                break;
            case CPU_GPU:
                var cpuToBenchmark = cpuBenchmark(blenderExecutable,
                        taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion);
                benchmarks.add(cpuToBenchmark);

                for (GPU gpu : selectedGPUs) {
                    var gpuToBenchmark = gpuBenchmark(gpu, blenderExecutable,
                            taskFrameInfo, taskServerInfo, benchmarkFile, blenderVersion);
                    benchmarks.add(gpuToBenchmark);
                }
        }
        renderTaskRepository.saveAll(benchmarks);
    }

    private RenderTask cpuBenchmark(String blenderExecutable,
                                    TaskFrameInfo taskFrameInfo,
                                    TaskServerInfo taskServerInfo, String benchmarkFile, String blenderVersion) {
        var taskScriptInfo = TaskScriptInfo.builder()
                .taskResolutionX(800)
                .taskResolutionY(600)
                .taskResPercentage(50)
                .samples(50)
                .computeOn(ComputeOn.CPU)
                .deviceType(DeviceType.CPU)
                .deviceIDs(new ArrayList<>())
                .taskTileSize(PropertiesUtils.getCPUTileSize())
                .blenderEngine(BlenderEngine.CYCLES)
                .cores(PropertiesUtils.getSelectedCores())
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        var benchmarkID = UUID.randomUUID().toString();
        return RenderTask.builder()
                .blenderExecutable(blenderExecutable)
                .frameInfo(taskFrameInfo)
                .scriptInfo(taskScriptInfo)
                .serverInfo(taskServerInfo)
                .taskBlendFile(benchmarkFile)
                .benchmark(true)
                .projectName("CPU Benchmark " + benchmarkID)
                .projectID(benchmarkID)
                .blenderVersion(blenderVersion)
                .taskID(UUID.randomUUID().toString())
                .taskDir(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR))
                .build();
    }

    private RenderTask gpuBenchmark(GPU gpu, String blenderExecutable,
                                    TaskFrameInfo taskFrameInfo,
                                    TaskServerInfo taskServerInfo, String benchmarkFile, String blenderVersion) {
        var deviceIDList = new ArrayList<String>();
        deviceIDList.add(gpu.getGpuID());
        var taskScriptInfo = TaskScriptInfo.builder()
                .taskResolutionX(800)
                .taskResolutionY(600)
                .taskResPercentage(50)
                .samples(50)
                .computeOn(ComputeOn.GPU)
                .deviceType(gpu.getDeviceType())
                .deviceIDs(deviceIDList)
                .taskTileSize(PropertiesUtils.getGPUTileSize())
                .blenderEngine(BlenderEngine.CYCLES)
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        var benchmarkID = UUID.randomUUID().toString();
        return RenderTask.builder()
                .blenderExecutable(blenderExecutable)
                .frameInfo(taskFrameInfo)
                .scriptInfo(taskScriptInfo)
                .serverInfo(taskServerInfo)
                .taskBlendFile(benchmarkFile)
                .benchmark(true)
                .projectName(gpu.getGpuID() + " Benchmark " + benchmarkID)
                .projectID(benchmarkID)
                .blenderVersion(blenderVersion)
                .taskID(UUID.randomUUID().toString())
                .taskDir(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR))
                .build();

    }

}
