package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Profile({"SERVER", "DUAL"})
@Service
public class ServerQueueServiceImpl implements ServerQueueService {
    private BlockingQueue<RenderTask> serverQueue;
    private final NodeRepository nodeRepository;

    public ServerQueueServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void addRenderTasksToServerQueue(Project project) {
        // TODO Work needs to be done to handle the following.  The total number of queue items will be equal to the number of parts x number of frames
        // TODO This will apply to both animations and still images.  For Still Images that don't have parts then the frame will be 1.
        var queueReady = true;
        var serverInfo = TaskServerInfo.builder().systemID(PropertiesUtils.getSystemID()).build();

        while (queueReady) {
            TaskFrameInfo frameInfo;
            if (project.getProjectSettings().isUseParts()) {
                var parts = project.getProjectSettings().getPartsPerFrame();

                frameInfo = TaskFrameInfo.builder()
                        .frameNumber(null)
                        .partNumber(null)
                        .build();

            } else {
                frameInfo = TaskFrameInfo.builder()
                        .frameNumber(project.getProjectStatus().getQueueIndex())
                        .build();

            }
            var renderTask = RenderTask.builder()
                    .projectID(project.getProjectID())
                    .projectName(project.getProjectName())
                    .useParts(project.getProjectSettings().isUseParts())
                    .serverInfo(serverInfo)
                    .frameInfo(frameInfo)
                    .build();

            queueReady = serverQueue.offer(renderTask);
        }

    }

    @Override
    public void updateQueueLimit() {
        var slots = 0;

        var nodes = nodeRepository.findNodesByBenchmarkCompleteTrueAndActiveTrue();

        for (Node node : nodes) {
            slots += node.getTotalRenderingSlots();
        }

        var queueSize = slots * 2;


        if (serverQueue != null) {
            BlockingQueue<RenderTask> tempQueue = new LinkedBlockingQueue<>(queueSize);
            for (RenderTask task : serverQueue) {
                tempQueue.add(task);
            }
            serverQueue = tempQueue;
        } else {
            serverQueue = new LinkedBlockingQueue<>(queueSize);
        }

    }
}
