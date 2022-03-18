package com.dryadandnaiad.sethlans.services;

import org.springframework.scheduling.annotation.Async;

public interface RenderService {

    @Async
    void retrievePendingRenderTasks();

    @Async
    void executeRenders();

}
