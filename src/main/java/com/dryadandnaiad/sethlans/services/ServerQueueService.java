package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;

import java.util.List;

public interface ServerQueueService {
    RenderTask retrieveRenderTaskFromPendingQueue(NodeType nodeType);

    void addRenderTasksToPendingQueue(Project project);

    RenderTask retrieveRenderTaskFromCompletedQueue();

    boolean addRenderTasksToCompletedQueue(RenderTask renderTask);

    void updatePendingQueueLimit();

    void resetPendingRenderTaskQueue();

    List<RenderTask> listCurrentTasksInQueue();
}
