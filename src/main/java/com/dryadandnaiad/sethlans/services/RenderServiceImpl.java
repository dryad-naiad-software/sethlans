package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.blender.BlenderScript;
import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.blender.BlenderExecutable;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@Profile({"NODE", "DUAL"})
public class RenderServiceImpl implements RenderService {

    private final RenderTaskRepository renderTaskRepository;

    public RenderServiceImpl(RenderTaskRepository renderTaskRepository) {
        this.renderTaskRepository = renderTaskRepository;
    }

    @Async
    @Override
    public void retrievePendingRenderTasks() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.debug(e.getMessage());
        }
        while (true) {
            if (!PropertiesUtils.isNodePaused()) {
                var server = PropertiesUtils.getAuthorizedServer();
                if (server != null && server.isBenchmarkComplete()) {
                    var totalSlot = PropertiesUtils.getTotalNodeSlots();
                    var pendingRenderTasks =
                            renderTaskRepository.findRenderTasksByBenchmarkIsFalseAndCompleteIsFalse();
                    if (pendingRenderTasks.size() < totalSlot) {
                        var params = ImmutableMap.<String, String>builder()
                                .put("system-id", PropertiesUtils.getSystemID())
                                .build();
                        var path = "/api/v1/server_queue/retrieve_task";
                        var host = server.getIpAddress();
                        var port = server.getNetworkPort();
                        var renderTaskJson = NetworkUtils.getJSONWithParams(path, host, port,
                                params, true);
                        if (renderTaskJson == null || renderTaskJson.isEmpty()) {
                            log.debug("No tasks present on server.");
                        } else {
                            var objectMapper = new ObjectMapper();
                            try {
                                var renderTask = objectMapper
                                        .readValue(renderTaskJson, RenderTask.class);
                                if (!blenderVersionCheck(renderTask.getBlenderVersion(), server)) {
                                    throw new Exception("Blender version not present on node or server");
                                }
                                setBlenderExecutable(renderTask);
                                renderTask.setTaskDir(ConfigUtils.getProperty(ConfigKeys.CACHE_DIR) + File.separator
                                        + renderTask.getTaskID());
                                new File(renderTask.getTaskDir()).mkdirs();
                                getBlendFile(renderTask, server);
                                BlenderUtils.setImageFileName(renderTask);
                                renderTask.setNodeID(PropertiesUtils.getSystemID());
                                renderTaskRepository.save(renderTask);
                                log.debug("Render Task added to node:");
                                log.debug(renderTask.getTaskID());
                                log.debug(renderTask.toString());
                            } catch (Exception e) {
                                log.error(e.getMessage());
                                log.error(Throwables.getStackTraceAsString(e));
                            }
                        }

                    }
                }

            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }


    }

    @Async
    @Override
    public void executeRenders() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            log.debug(e.getMessage());
        }
        while (true) {
            if (!PropertiesUtils.isNodePaused()) {
                var server = PropertiesUtils.getAuthorizedServer();
                if (server != null && server.isBenchmarkComplete()) {
                    var renderTasksToExecute =
                            renderTaskRepository.findRenderTasksByBenchmarkIsFalseAndInProgressIsFalseAndCompleteIsFalse();
                    if (renderTasksToExecute.size() > 0) {
                        var activeRenders =
                                renderTaskRepository.findRenderTaskByBenchmarkIsFalseAndInProgressIsTrueAndCompleteIsFalse();
                        var slotsInUse = activeRenders.size();
                        var totalSlots = PropertiesUtils.getTotalNodeSlots();
                        if (slotsInUse < totalSlots) {
                            if (!renderTasksToExecute.isEmpty()) {
                                renderTaskRepository.save(assignComputeMethod(renderTasksToExecute.get(0), activeRenders));
                                var renderTask =
                                        renderTaskRepository.getRenderTaskByTaskID(renderTasksToExecute.get(0).getTaskID());
                                if (BlenderScript.writeRenderScript(renderTask)) {
                                    new Thread(() -> {
                                        renderTask
                                                .setRenderTime(BlenderUtils.executeRenderTask
                                                        (renderTask, false));
                                        if (renderTask.getRenderTime() != null) {
                                            renderTask.setInProgress(false);
                                            renderTask.setTaskImageFileMD5Sum(
                                                    FileUtils.getMD5ofFile(new File(renderTask.getTaskDir()
                                                            + File.separator
                                                            + renderTask.getTaskImageFile())));
                                            renderTask.setComplete(true);
                                            renderTaskRepository.save(renderTask);
                                        }
                                    }).start();


                                }
                            }
                        }
                    }
                }


            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }
    }

    @Async
    @Override
    public void sendCompletedRendersToServers() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            log.debug(e.getMessage());
        }
        while (true) {
            var server = PropertiesUtils.getAuthorizedServer();
            if (server != null && server.isBenchmarkComplete()) {
                var tasksToSend =
                        renderTaskRepository.findRenderTasksByBenchmarkIsFalseAndInProgressIsFalseAndCompleteIsTrueAndSentToServerIsFalse();
                if (tasksToSend.size() > 0) {
                    for (RenderTask renderTask : tasksToSend) {
                        try {
                            var path = "/api/v1/server_queue/receive_task";
                            var host = server.getIpAddress();
                            var port = server.getNetworkPort();
                            var objectMapper = new ObjectMapper();
                            var taskJson = objectMapper
                                    .writeValueAsString(renderTask);
                            var accepted = NetworkUtils.postJSONToURL(path, host, port, taskJson, true);
                            if (accepted) {
                                renderTask.setSentToServer(true);
                                renderTaskRepository.save(renderTask);
                            }

                        } catch (JsonProcessingException e) {
                            log.error(e.getMessage());
                            log.error(Throwables.getStackTraceAsString(e));
                        }
                    }

                }
            }


            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }

    }

    private RenderTask assignComputeMethod(RenderTask renderTaskToAssign, List<RenderTask> activeRenders) {
        var nodeType = PropertiesUtils.getNodeType();
        var selectedGPUs = PropertiesUtils.getSelectedGPUs();
        var combinedGPU = PropertiesUtils.isGPUCombined();
        log.debug("Assigning compute method to " + renderTaskToAssign.getTaskID());
        var inUseIds = new ArrayList<String>();

        for (RenderTask renderTask : activeRenders) {
            if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU)) {
                inUseIds.add(renderTask.getScriptInfo().getDeviceIDs().get(0));
            }
        }

        switch (nodeType) {
            case CPU_GPU -> {
                var cpuInUse = false;
                var gpuInUse = false;
                for (RenderTask renderTask : activeRenders) {
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU) && combinedGPU) {
                        gpuInUse = true;
                        log.debug("GPU In Use");
                    } else if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU)
                            && selectedGPUs.size() == 1) {
                        gpuInUse = true;
                        log.debug("GPU In Use");
                    } else if (inUseIds.size() == selectedGPUs.size()) {
                        gpuInUse = true;
                        log.debug("All GPU In Use");
                    }
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.CPU)) {
                        cpuInUse = true;
                        log.debug("CPU In Use");
                    }
                }
                if (!gpuInUse) {
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
                    renderTaskToAssign.getScriptInfo().setTaskTileSize(PropertiesUtils.getGPUTileSize());
                } else if (!cpuInUse) {
                    log.debug("Assigning to CPU");
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
                    renderTaskToAssign.getScriptInfo().setCores(PropertiesUtils.getSelectedCores());
                    renderTaskToAssign.getScriptInfo().setTaskTileSize(PropertiesUtils.getCPUTileSize());

                }
            }
            case GPU -> {
                renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
                renderTaskToAssign.getScriptInfo().setTaskTileSize(PropertiesUtils.getGPUTileSize());
            }
            case CPU -> {
                renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
                renderTaskToAssign.getScriptInfo().setCores(PropertiesUtils.getSelectedCores());
                renderTaskToAssign.getScriptInfo().setTaskTileSize(PropertiesUtils.getCPUTileSize());
            }
        }

        if (renderTaskToAssign.getScriptInfo().getComputeOn().equals(ComputeOn.GPU)) {
            var ids = new ArrayList<String>();
            if (selectedGPUs.size() > 1) {
                for (GPU gpu : selectedGPUs) {
                    ids.add(gpu.getGpuID());
                }
            } else {
                ids.add(selectedGPUs.get(0).getGpuID());
            }
            log.debug("Current GPU Id's on System "
                    + ids);
            if (combinedGPU || selectedGPUs.size() == 1) {
                renderTaskToAssign.getScriptInfo().setDeviceType(selectedGPUs.get(0).getDeviceType());
                renderTaskToAssign.getScriptInfo().setDeviceIDs(ids);
            } else {
                var freeIds = new ArrayList<>(ids);
                freeIds.removeAll(inUseIds);
                log.debug("Current GPU ID's available " + freeIds);
                if (freeIds.size() > 0) {
                    for (GPU gpu : selectedGPUs) {
                        if (freeIds.get(0).equals(gpu.getGpuID())) {
                            renderTaskToAssign.getScriptInfo().setDeviceType(gpu.getDeviceType());
                        }
                    }
                    renderTaskToAssign.getScriptInfo().setDeviceIDs(Collections.singletonList(freeIds.get(0)));
                }
            }
        }

        renderTaskToAssign.setInProgress(true);
        log.debug(renderTaskToAssign.toString());
        return renderTaskToAssign;
    }


    private void getBlendFile(RenderTask renderTask, Server server) throws Exception {
        var cachedProjectFile = new
                File(ConfigUtils.getProperty(ConfigKeys.BLEND_FILE_CACHE_DIR)
                + File.separator + renderTask.getProjectID()
                + File.separator + renderTask.getTaskBlendFile());
        if (!cachedProjectFile.exists()) {
            var projectFile = BlenderUtils.downloadProjectFileFromServer(renderTask,
                    server);
            if (projectFile == null) {
                throw new Exception("Unable to download project file");
            } else {
                renderTask.setTaskBlendFile(projectFile);
            }

        } else {
            renderTask.setTaskBlendFile(cachedProjectFile.toString());
        }
    }

    private void setBlenderExecutable(RenderTask renderTask) {
        var blenderVersion = renderTask.getBlenderVersion();
        var blenderList = PropertiesUtils.getInstalledBlenderExecutables();
        for (BlenderExecutable blender : blenderList) {
            if (blender.getBlenderVersion().equals(blenderVersion)) {
                renderTask.setBlenderExecutable(blender.getBlenderExecutable());
            }
        }
    }

    private boolean blenderVersionCheck(String version, Server server) {
        var blenderList = PropertiesUtils.getInstalledBlenderExecutables();
        for (BlenderExecutable blender : blenderList) {
            if (blender.getBlenderVersion().equals(version)) {
                return true;
            }
        }
        return BlenderUtils.requestBlenderFromServer(version, server);
    }
}
