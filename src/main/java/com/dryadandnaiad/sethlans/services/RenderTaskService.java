package com.dryadandnaiad.sethlans.services;

import org.springframework.scheduling.annotation.Async;

public interface RenderTaskService {

    @Async
    void retrievePendingRenderTasks();

}
