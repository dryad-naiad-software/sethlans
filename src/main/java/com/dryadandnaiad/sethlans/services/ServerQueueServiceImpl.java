package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.ProjectState;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskScriptInfo;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskServerInfo;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.utils.ImageUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Profile({"SERVER", "DUAL"})
@Service
public class ServerQueueServiceImpl implements ServerQueueService {
    private BlockingQueue<RenderTask> pendingRenderQueue;
    private final ProjectRepository projectRepository;
    private final NodeRepository nodeRepository;
    private final BlockingQueue<RenderTask> completedRenderQueue;

    public ServerQueueServiceImpl(ProjectRepository projectRepository, NodeRepository nodeRepository) {
        this.projectRepository = projectRepository;
        this.nodeRepository = nodeRepository;
        this.completedRenderQueue = new LinkedBlockingQueue<>(PropertiesUtils.getServerCompleteQueueSize());
    }

    @Override
    public RenderTask retrieveRenderTaskFromPendingQueue(NodeType nodeType) {
        try {
            var task = pendingRenderQueue.peek();
            if (task != null) {
                var project = projectRepository.getProjectByProjectID(task.getProjectID()).get();
                switch (nodeType) {
                    case CPU_GPU:
                        return pendingRenderQueue.poll(5, TimeUnit.SECONDS);
                    case CPU:
                        if (project.getProjectSettings().getComputeOn() != ComputeOn.GPU) {
                            return pendingRenderQueue.poll(5, TimeUnit.SECONDS);
                        }
                        return null;
                    case GPU:
                        if (project.getProjectSettings().getComputeOn() != ComputeOn.CPU) {
                            return pendingRenderQueue.poll(5, TimeUnit.SECONDS);
                        }
                        return null;
                }
            }
            return null;


        } catch (InterruptedException e) {
            log.debug(e.getMessage());
            return null;
        }
    }


    @Override
    public void addRenderTasksToPendingQueue(Project project) {
        log.debug("Attempting to add render tasks to server pending queue. There are "
                + pendingRenderQueue.size() + " items.");

        var queueReady = true;

        var serverInfo = TaskServerInfo.builder().systemID(PropertiesUtils.getSystemID()).build();

        while (queueReady && project.getProjectStatus().getRemainingQueueSize() > 0) {

            TaskFrameInfo frameInfo;
            if (project.getProjectSettings().isUseParts()) {
                var parts = project.getProjectSettings().getPartsPerFrame();
                Integer frameNumber;
                Integer partNumber;
                if (project.getProjectStatus().getQueueIndex() == 0) {
                    frameNumber = project.getProjectSettings().getStartFrame();
                    partNumber = 1;
                } else {
                    frameNumber = project.getProjectStatus().getCurrentFrame();
                    partNumber = project.getProjectStatus().getCurrentPart();
                    if (Objects.equals(partNumber, parts)) {
                        partNumber = 1;
                        frameNumber = frameNumber + project.getProjectSettings().getStepFrame();
                    } else {
                        partNumber = partNumber + 1;
                    }
                }
                project.getProjectStatus().setCurrentFrame(frameNumber);
                project.getProjectStatus().setCurrentPart(partNumber);

                var partCoordinates = ImageUtils.configurePartCoordinates(parts);

                frameInfo = partCoordinates.get(partNumber-1);
                frameInfo.setFrameNumber(frameNumber);
                frameInfo.setPartNumber(partNumber);

            } else {
                Integer frameNumber;
                if (project.getProjectStatus().getQueueIndex() == 0) {
                    frameNumber = project.getProjectSettings().getStartFrame();
                } else {
                    frameNumber = project.getProjectStatus().getCurrentFrame()
                            + project.getProjectSettings().getStepFrame();
                }
                project.getProjectStatus().setCurrentFrame(frameNumber);
                frameInfo = TaskFrameInfo.builder()
                        .frameNumber(frameNumber)
                        .build();

            }
            TaskScriptInfo scriptInfo = TaskScriptInfo.builder()
                    .blenderEngine(project.getProjectSettings().getBlenderEngine())
                    .imageOutputFormat(project.getProjectSettings().getImageSettings().getImageOutputFormat())
                    .samples(project.getProjectSettings().getSamples())
                    .taskResolutionX(project.getProjectSettings().getImageSettings().getResolutionX())
                    .taskResolutionY(project.getProjectSettings().getImageSettings().getResolutionY())
                    .taskResPercentage(project.getProjectSettings().getImageSettings().getResPercentage())
                    .build();
            var renderTask = RenderTask.builder()
                    .projectID(project.getProjectID())
                    .projectName(project.getProjectName())
                    .blenderVersion(project.getProjectSettings().getBlenderVersion())
                    .useParts(project.getProjectSettings().isUseParts())
                    .taskID(UUID.randomUUID().toString())
                    .serverInfo(serverInfo)
                    .frameInfo(frameInfo)
                    .scriptInfo(scriptInfo)
                    .build();

            queueReady = pendingRenderQueue.offer(renderTask);
            if (queueReady) {
                if (project.getProjectStatus().getProjectState().equals(ProjectState.ADDED)) {
                    project.getProjectStatus().setProjectState(ProjectState.PENDING);
                }
                project.getProjectStatus().setQueueIndex(project.getProjectStatus().getQueueIndex() + 1);
                project.getProjectStatus().setRemainingQueueSize(project.getProjectStatus().getRemainingQueueSize() - 1);
                var remainingQueueSize = project.getProjectStatus().getRemainingQueueSize();
                log.debug("project remaining queue size " + remainingQueueSize);
                projectRepository.save(project);
                if (remainingQueueSize <= 0) {
                    break;
                }
            }
        }
        log.debug("Adding of render tasks has completed. " +
                "Server pending task queue has " + pendingRenderQueue.size() + " items.");
        log.debug(pendingRenderQueue.toString());

    }


    @Override
    public RenderTask retrieveRenderTaskFromCompletedQueue() {
        try {
            return completedRenderQueue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.debug(e.getMessage());
            return null;
        }
    }

    @Override
    public boolean addRenderTasksToCompletedQueue(RenderTask renderTask) {
        log.debug("Attempting to add render tasks to server completed queue. There are "
                + completedRenderQueue.size() + " items.");
        var taskAdded = completedRenderQueue.offer(renderTask);
        if (taskAdded) {
            log.debug(completedRenderQueue.toString());
            var project = projectRepository.getProjectByProjectID(renderTask.getProjectID()).get();
            if (project.getProjectStatus().getProjectState().equals(ProjectState.RENDERING)) {
                addRenderTasksToPendingQueue(project);
            }
            if (project.getProjectStatus().getProjectState().equals(ProjectState.STARTED)) {
                project.getProjectStatus().setProjectState(ProjectState.RENDERING);
                projectRepository.save(project);
            }

        }
        log.debug("Adding of render tasks has completed. " +
                "Server completed task queue has " + completedRenderQueue.size() + " items.");
        return taskAdded;
    }

    @Override
    public int currentNumberOfTasksInCompleteQueue() {
        return completedRenderQueue.size();
    }

    @Override
    public void updatePendingQueueLimit() {
        var slots = 0;

        var nodes = nodeRepository.findNodesByBenchmarkCompleteTrueAndActiveTrue();

        for (Node node : nodes) {
            slots += node.getTotalRenderingSlots();
        }

        var queueSize = slots * 2;


        if (pendingRenderQueue != null) {
            BlockingQueue<RenderTask> tempQueue = new LinkedBlockingQueue<>(queueSize);
            pendingRenderQueue.drainTo(tempQueue);
            pendingRenderQueue = tempQueue;
            log.debug("Server pending render queue updated, queue size: " + queueSize);

        } else {
            pendingRenderQueue = new LinkedBlockingQueue<>(queueSize);
            log.debug("Server pending render queue created, queue size: " + queueSize);
        }

    }

    @Override
    public void resetPendingRenderTaskQueue() {
        log.debug("Resetting server pending render task queue");
        var slots = 0;

        var nodes = nodeRepository.findNodesByBenchmarkCompleteTrueAndActiveTrue();

        for (Node node : nodes) {
            slots += node.getTotalRenderingSlots();
        }

        var queueSize = slots * 2;
        pendingRenderQueue = new LinkedBlockingQueue<>(queueSize);

    }

    @Override
    public List<RenderTask> listCurrentTasksInQueue() {
        return pendingRenderQueue.stream().toList();
    }

}
