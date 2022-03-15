package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
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
        while(true) {
            if(!PropertiesUtils.isNodePaused()) {
                var servers = serverRepository.findServersByBenchmarkCompleteTrue();
                if (servers.size() > 0) {
                    var totalSlot = PropertiesUtils.getTotalNodeSlots();
                    log.debug("Testing");
                    var pendingRenderTasks = renderTaskRepository.findRenderTasksByBenchmarkIsFalseAndCompleteIsFalse();

                    if(pendingRenderTasks.size() < totalSlot) {
                        log.debug("Total number of slots " + totalSlot);
                        log.debug("Total number of tasks " + pendingRenderTasks.size());
                    }
                }
            }
            Thread.sleep(5000);
        }







    }
}
