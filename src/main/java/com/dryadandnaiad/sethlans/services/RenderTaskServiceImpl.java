package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile({"NODE", "DUAL"})
public class RenderTaskServiceImpl implements RenderTaskService {

    private final ServerRepository serverRepository;
    public RenderTaskServiceImpl(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Async
    @Override
    public void retrievePendingRenderTasks() {

        var servers = serverRepository.findServersByBenchmarkCompleteTrue();


    }
}
