package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.blender.BlenderExecutable;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
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

    private final ServerRepository serverRepository;
    private final RenderTaskRepository renderTaskRepository;

    public RenderServiceImpl(ServerRepository serverRepository, RenderTaskRepository renderTaskRepository) {
        this.serverRepository = serverRepository;
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
                var servers = serverRepository.findServersByBenchmarkCompleteTrue();
                if (servers.size() > 0) {
                    for (Server server : servers) {
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
                                    if(!blenderVersionCheck(renderTask.getBlenderVersion(), server)){
                                        throw new Exception("Blender version not present on node or server");
                                    }
                                    setBlenderExecutable(renderTask);
                                    renderTask.setTaskDir(ConfigUtils.getProperty(ConfigKeys.TEMP_DIR) + File.separator
                                            + renderTask.getTaskID());
                                    var cachedProjectFile = new
                                            File(ConfigUtils.getProperty(ConfigKeys.BLEND_FILE_CACHE_DIR)
                                            + File.separator + renderTask.getProjectID()
                                            + File.separator + renderTask.getTaskBlendFile());
                                    if(!cachedProjectFile.exists()) {
                                        var projectFile = BlenderUtils.downloadProjectFileFromServer(renderTask,
                                                server);
                                        if(projectFile == null) {
                                            throw new Exception("Unable to download project file");
                                        } else {
                                            renderTask.setTaskBlendFile(projectFile);
                                        }

                                    } else {
                                        renderTask.setTaskBlendFile(cachedProjectFile.toString());
                                    }
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
                var servers = serverRepository.findServersByBenchmarkCompleteTrue();
                var renderTasksToExecute =
                        renderTaskRepository.findRenderTasksByBenchmarkIsFalseAndInProgressIsFalseAndCompleteIsFalse();
                if (servers.size() > 0 && renderTasksToExecute.size() > 0) {
                    var activeRenders =
                            renderTaskRepository.findRenderTaskByBenchmarkIsFalseAndInProgressIsTrueAndCompleteIsFalse();
                    var slotsInUse = activeRenders.size();
                    var totalSlots = PropertiesUtils.getTotalNodeSlots();
                    if (slotsInUse < totalSlots) {
                        if (!renderTasksToExecute.isEmpty()) {
                            renderTaskRepository.save(assignComputeMethod(renderTasksToExecute.get(0), activeRenders));
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

    private RenderTask assignComputeMethod(RenderTask renderTaskToAssign, List<RenderTask> activeRenders) {
        var nodeType = PropertiesUtils.getNodeType();
        var selectedGPUs = PropertiesUtils.getSelectedGPUs();
        var combinedGPU = PropertiesUtils.isGPUCombined();
        log.debug("Assigning compute method to " + renderTaskToAssign.getTaskID());

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
                    }
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.CPU)) {
                        cpuInUse = true;
                        log.debug("CPU In Use");
                    }
                }
                if (!gpuInUse) {
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
                } else if (!cpuInUse) {
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
                    renderTaskToAssign.getScriptInfo().setCores(PropertiesUtils.getSelectedCores());
                }
            }
            case GPU -> renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
            case CPU ->  {
                renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
                renderTaskToAssign.getScriptInfo().setCores(PropertiesUtils.getSelectedCores());
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
            if (combinedGPU || selectedGPUs.size() == 1) {
                renderTaskToAssign.getScriptInfo().setDeviceType(selectedGPUs.get(0).getDeviceType());
                renderTaskToAssign.getScriptInfo().setDeviceIDs(ids);
            } else {
                renderTaskToAssign.getScriptInfo().setDeviceType(selectedGPUs.get(0).getDeviceType());
                var inUseIds = new ArrayList<String>();
                for (RenderTask renderTask : activeRenders) {
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU)) {
                        inUseIds.add(renderTask.getScriptInfo().getDeviceIDs().get(0));
                    }
                }
                var freeIds = new ArrayList<>(ids);
                freeIds.removeAll(inUseIds);
                renderTaskToAssign.getScriptInfo().setDeviceIDs(Collections.singletonList(freeIds.get(0)));
            }
        }

        renderTaskToAssign.setInProgress(true);
        log.debug(renderTaskToAssign.toString());
        return renderTaskToAssign;
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
