package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.ProjectState;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Profile({"SERVER", "DUAL"})
@Service
public class ServerQueueServiceImpl implements ServerQueueService {
    private BlockingQueue<RenderTask> serverQueue;
    private final ProjectRepository projectRepository;
    private final NodeRepository nodeRepository;

    public ServerQueueServiceImpl(ProjectRepository projectRepository, NodeRepository nodeRepository) {
        this.projectRepository = projectRepository;
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void addRenderTasksToServerQueue(Project project) {
        var queueReady = true;
        var serverInfo = TaskServerInfo.builder().systemID(PropertiesUtils.getSystemID()).build();

        while (queueReady) {
            TaskFrameInfo frameInfo;
            if (project.getProjectSettings().isUseParts()) {
                var parts = project.getProjectSettings().getPartsPerFrame();
                Integer frameNumber;
                Integer partNumber;
                if (project.getProjectStatus().getQueueIndex() == 0) {
                    frameNumber = 1;
                    partNumber = 1;
                } else {
                    frameNumber = project.getProjectStatus().getCurrentFrame();
                    partNumber = project.getProjectStatus().getCurrentPart();
                    if (Objects.equals(partNumber, parts)) {
                        partNumber = 1;
                        frameNumber = frameNumber + 1;
                    } else {
                        partNumber = partNumber + 1;
                    }
                }
                project.getProjectStatus().setCurrentFrame(frameNumber);
                project.getProjectStatus().setCurrentPart(partNumber);

                frameInfo = TaskFrameInfo.builder()
                        .frameNumber(frameNumber)
                        .partNumber(partNumber)
                        .build();

            } else {
                Integer frameNumber;
                if (project.getProjectStatus().getQueueIndex() == 0) {
                    frameNumber = 1;
                } else {
                    frameNumber = project.getProjectStatus().getQueueIndex();
                }
                project.getProjectStatus().setCurrentFrame(frameNumber);
                frameInfo = TaskFrameInfo.builder()
                        .frameNumber(frameNumber)
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
            if (queueReady) {
                if(project.getProjectStatus().getProjectState().equals(ProjectState.ADDED)) {
                    project.getProjectStatus().setProjectState(ProjectState.PENDING);
                }
                project.getProjectStatus().setQueueIndex(project.getProjectStatus().getQueueIndex() + 1);
                project.getProjectStatus().setRemainingQueueSize(project.getProjectStatus().getRemainingQueueSize() - 1);
                projectRepository.save(project);
            }
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
            tempQueue.addAll(serverQueue);
            serverQueue = tempQueue;
            log.debug("Server queue updated, queue size: " + queueSize);

        } else {
            serverQueue = new LinkedBlockingQueue<>(queueSize);
            log.debug("Server queue created, queue size: " + queueSize);
        }

    }
}
