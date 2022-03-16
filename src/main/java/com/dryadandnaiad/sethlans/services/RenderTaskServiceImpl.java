package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
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

@Service
@Slf4j
@Profile({"NODE", "DUAL"})
public class RenderTaskServiceImpl implements RenderTaskService {

    private final ServerRepository serverRepository;
    private final RenderTaskRepository renderTaskRepository;

    public RenderTaskServiceImpl(ServerRepository serverRepository, RenderTaskRepository renderTaskRepository) {
        this.serverRepository = serverRepository;
        this.renderTaskRepository = renderTaskRepository;
    }

    @Async
    @Override
    public void retrievePendingRenderTasks() throws InterruptedException {
        Thread.sleep(20000);
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
                            var objectMapper = new ObjectMapper();
                            try {
                                var renderTask = objectMapper
                                        .readValue(renderTaskJson, RenderTask.class);
                                renderTaskRepository.save(renderTask);
                            } catch (JsonProcessingException e) {
                                log.error(e.getMessage());
                                log.error(Throwables.getStackTraceAsString(e));
                            }
                        }

                    }
                }
            }
            Thread.sleep(5000);
        }


    }
}
