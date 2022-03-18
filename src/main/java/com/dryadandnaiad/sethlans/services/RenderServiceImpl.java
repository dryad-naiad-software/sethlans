package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
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
                            var renderTaskJson = NetworkUtils.getJSONWithParams(path, host, port, params, true);
                            if (renderTaskJson == null || renderTaskJson.isEmpty()) {
                                log.debug("No tasks present on server.");
                            } else {
                                var objectMapper = new ObjectMapper();
                                try {
                                    var renderTask = objectMapper
                                            .readValue(renderTaskJson, RenderTask.class);
                                    renderTaskRepository.save(renderTask);
                                    log.debug("Render Task added to node:");
                                    log.debug(renderTask.toString());
                                } catch (JsonProcessingException e) {
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
                log.debug(renderTasksToExecute.toString());
                if (servers.size() > 0 && renderTasksToExecute.size() > 0) {
                    var activeRenders =
                            renderTaskRepository.findRenderTaskByBenchmarkIsFalseAndInProgressIsTrueAndCompleteIsFalse();
                    var slotsInUse = activeRenders.size();
                    var totalSlots = PropertiesUtils.getTotalNodeSlots();
                    if (slotsInUse < totalSlots) {
                        if (!renderTasksToExecute.isEmpty()) {
                            renderTaskRepository.save(assignComputeMethod(renderTasksToExecute.get(0), activeRenders));
                        }
                        activeRenders = renderTaskRepository.findRenderTaskByBenchmarkIsFalseAndInProgressIsTrueAndCompleteIsFalse();
                        log.debug(activeRenders.toString());
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

        switch (nodeType) {
            case CPU_GPU -> {
                var cpuInUse = false;
                var gpuInUse = false;
                for (RenderTask renderTask : activeRenders) {
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU) && combinedGPU) {
                        gpuInUse = true;
                    }
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.CPU)) {
                        cpuInUse = true;
                    }
                }
                if (!gpuInUse) {
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
                } else if (!cpuInUse) {
                    renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
                }
            }
            case GPU -> renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.GPU);
            case CPU -> renderTaskToAssign.getScriptInfo().setComputeOn(ComputeOn.CPU);
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
                renderTaskToAssign.setInProgress(true);
            }
        }


        return renderTaskToAssign;
    }
}
