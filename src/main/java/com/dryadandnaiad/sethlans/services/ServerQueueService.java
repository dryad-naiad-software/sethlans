package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;

public interface ServerQueueService {
    RenderTask retrieveRenderTaskFromServerQueue();

    void addRenderTasksToServerQueue(Project project);

    void updateQueueLimit();

    void resetRenderTaskQueue();
}
