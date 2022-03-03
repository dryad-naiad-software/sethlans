package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class ServerQueueServiceImpl implements ServerQueueService {
    private BlockingQueue<RenderTask> serverQueue;
    private final NodeRepository nodeRepository;

    public ServerQueueServiceImpl(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void addRenderTasksToServerQueue(Project project) {

    }

    @Override
    public void updateQueueLimit() {
        var slots = 0;

        var nodes = nodeRepository.findNodesByBenchmarkCompleteAndActive();

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
