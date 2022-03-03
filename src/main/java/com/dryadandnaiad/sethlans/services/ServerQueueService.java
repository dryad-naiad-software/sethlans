package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.blender.project.Project;

public interface ServerQueueService {
    void addRenderTasksToServerQueue(Project project);

    void updateQueueLimit();
}
