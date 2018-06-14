/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.services.queue;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.queue.*;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.QueueAction;
import com.dryadandnaiad.sethlans.services.database.*;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.dryadandnaiad.sethlans.services.queue.QueueNodeActions.*;
import static com.dryadandnaiad.sethlans.services.queue.QueueNodeStatusActions.*;
import static com.dryadandnaiad.sethlans.services.queue.QueueProcessActions.*;
import static com.dryadandnaiad.sethlans.services.queue.QueueProjectActions.queueProjectActions;

/**
 * /**
 * Created Mario Estrella on 4/21/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class QueueServiceImpl implements QueueService {
    private static final Logger LOG = LoggerFactory.getLogger(QueueServiceImpl.class);
    private RenderQueueDatabaseService renderQueueDatabaseService;
    private boolean modifyingQueue = false;
    private ProcessImageAndAnimationService processImageAndAnimationService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private ProcessQueueDatabaseService processQueueDatabaseService;
    private ProcessFrameDatabaseService processFrameDatabaseService;
    private List<ProcessNodeStatus> nodeStatuses = new ArrayList<>();
    private List<ProcessIdleNode> idleNodes = new ArrayList<>();
    private List<QueueActionItem> queueActionItemList = new ArrayList<>();
    private List<ProcessQueueItem> incomingQueueItemList = new ArrayList<>();
    private Set<NodeOnlineItem> nodeOnlineItemList = new HashSet<>();
    private List<Long> nodesToDelete = new ArrayList<>();
    private List<Long> nodesToDisable = new ArrayList<>();
    private final static int QUEUE = 500;
    private final static int CLEANUP = 350;

    @Async
    @Override
    public void startQueue() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOG.debug("Stopping Sethlans Queue Service");
        }
        while (true) {
            try {
                Thread.sleep(100);
                projectActions();
                cleanQueue();
                populateQueue();
                assignmentWorkflow();
                projectActions();
                processingWorkflow();
                projectActions();
            } catch (InterruptedException e) {
                LOG.debug("Stopping Sethlans Queue Service");
                break;
            }
        }
    }

    private void assignmentWorkflow() {
        nodeOnlineStatus();
        processNodeDeletion();
        processDisablingNodes();
        freeIdleNode();
        assignQueueItemToNode();
        sendQueueItemsToAssignedNode();
        processNodeAcknowledgements();
    }

    private void processingWorkflow() {
        nodeOnlineStatus();
        processNodeDeletion();
        processDisablingNodes();
        incomingCompleteItems();
        processReceivedFiles();
        processImages();
        finishProject();
    }

    @Override
    public void queueIdleNode(String connection_uuid, ComputeType computeType) {
        idleNodes.add(new ProcessIdleNode(connection_uuid, computeType));
    }

    @Override
    public void addNodeToDisable(Long id) {
        nodesToDisable.add(id);
    }

    @Override
    public void addNodeToDeleteQueue(Long id) {
        nodesToDelete.add(id);
    }

    @Override
    public void nodeRejectQueueItem(String queue_uuid) {
        nodeStatuses.add(new ProcessNodeStatus(queue_uuid, false));
    }

    @Override
    public void nodeAcknowledgeQueueItem(String queue_uuid) {
        nodeStatuses.add(new ProcessNodeStatus(queue_uuid, true));
    }

    @Override
    public void nodeStatusUpdateItem(String connection_uuid, boolean online) {
        nodeOnlineItemList.add(new NodeOnlineItem(connection_uuid, online));
    }

    @Override
    public void addItemToProcess(ProcessQueueItem processQueueItem) {
        incomingQueueItemList.add(processQueueItem);
    }

    private void processNodeDeletion() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodesToDelete.size() > 0) {
                List<Long> idsReviewed = new ArrayList<>();
                for (Long id : new ArrayList<>(nodesToDelete)) {
                    deleteNodes(id, sethlansNodeDatabaseService, renderQueueDatabaseService, blenderProjectDatabaseService);
                    idsReviewed.add(id);
                }
                nodesToDelete.removeAll(idsReviewed);
            }
        }
        modifyingQueue = false;
    }

    private void processDisablingNodes() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodesToDisable.size() > 0) {
                List<Long> idsReviewed = new ArrayList<>();
                for (Long id : new ArrayList<>(nodesToDisable)) {
                    disableNodes(id, sethlansNodeDatabaseService, renderQueueDatabaseService, blenderProjectDatabaseService);
                    idsReviewed.add(id);
                }
                nodesToDisable.removeAll(idsReviewed);
            }
        }
        modifyingQueue = false;
    }

    private void incomingCompleteItems() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (incomingQueueItemList.size() > 0) {
                List<ProcessQueueItem> itemsReviewed = new ArrayList<>();
                for (ProcessQueueItem processQueueItem : new ArrayList<>(incomingQueueItemList)) {
                    processIncoming(itemsReviewed, processQueueItem, processQueueDatabaseService, renderQueueDatabaseService, sethlansNodeDatabaseService, blenderProjectDatabaseService);
                }
                incomingQueueItemList.removeAll(itemsReviewed);
            }

            modifyingQueue = false;
        }
    }


    private void projectActions() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (queueActionItemList.size() > 0) {
                List<QueueActionItem> processedAction = new ArrayList<>();
                for (QueueActionItem queueActionItem : new ArrayList<>(queueActionItemList)) {
                    queueProjectActions(queueActionItem, renderQueueDatabaseService,
                            blenderProjectDatabaseService, processQueueDatabaseService,
                            sethlansNodeDatabaseService, processedAction);
                }
                queueActionItemList.removeAll(processedAction);
            }
            modifyingQueue = false;
        }
    }

    private void freeIdleNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (idleNodes.size() > 0) {
                List<ProcessIdleNode> processedNodes = new ArrayList<>();
                for (ProcessIdleNode idleNode : new ArrayList<>(idleNodes)) {
                    processIdleNodes(sethlansNodeDatabaseService, idleNode,
                            renderQueueDatabaseService, blenderProjectDatabaseService, processedNodes, incomingQueueItemList);
                }
                idleNodes.removeAll(processedNodes);
            }
            modifyingQueue = false;
        }
    }

    private void nodeOnlineStatus() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodeOnlineItemList.size() > 0) {
                List<NodeOnlineItem> processedStatusNodes = new ArrayList<>();
                for (NodeOnlineItem nodeOnlineItem : new ArrayList<>(nodeOnlineItemList)) {
                    processOfflineNodes(sethlansNodeDatabaseService, nodeOnlineItem, renderQueueDatabaseService, blenderProjectDatabaseService, processedStatusNodes);
                }
                nodeOnlineItemList.removeAll(processedStatusNodes);
            }
            modifyingQueue = false;
        }
    }

    private void processNodeAcknowledgements() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodeStatuses.size() > 0) {
                List<ProcessNodeStatus> itemsProcessed = new ArrayList<>();
                for (ProcessNodeStatus processNodeStatus : new ArrayList<>(nodeStatuses)) {
                    try {
                        processAcknowledgements(processNodeStatus, renderQueueDatabaseService,
                                blenderProjectDatabaseService, sethlansNodeDatabaseService, itemsProcessed);
                    } catch (NullPointerException e) {
                        LOG.error("Node acknowledgement received before node was shutdown/removed.");
                    }

                }
                nodeStatuses.removeAll(itemsProcessed);
            }
            modifyingQueue = false;
        }
    }

    private void cleanQueue() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (renderQueueDatabaseService.listAll().size() > CLEANUP) {
                for (RenderQueueItem renderQueueItem : renderQueueDatabaseService.listAll()) {
                    if (renderQueueItem.isComplete()) {
                        renderQueueDatabaseService.delete(renderQueueItem);
                    }
                }
            }
            modifyingQueue = false;
        }
    }

    private void populateQueue() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (sethlansNodeDatabaseService.activeNodeList().size() > 0 && blenderProjectDatabaseService.listAll().size() > 0) {
                populateQueueRunningProjects();
                populateQueuePendingProjects();
            }
            modifyingQueue = false;
        }
    }

    private void populateQueuePendingProjects() {
        if (renderQueueDatabaseService.listPendingRender().size() < QUEUE && blenderProjectDatabaseService.getPendingProjects().size() > 0) {
            for (BlenderProject blenderProject : blenderProjectDatabaseService.getPendingProjects()) {
                int throttle;
                List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
                if (blenderFramePartList.size() <= QUEUE) {
                    throttle = blenderFramePartList.size();
                } else {
                    throttle = QUEUE - renderQueueDatabaseService.listPendingRender().size();
                }
                int queueIndex = blenderProject.getQueueIndex();
                addRenderQueueItem(blenderProject, throttle, queueIndex, blenderFramePartList);

            }
        }
    }

    private void populateQueueRunningProjects() {
        if (renderQueueDatabaseService.listPendingRender().size() < QUEUE && blenderProjectDatabaseService.getRemainingQueueProjects().size() > 0) {
            for (BlenderProject blenderProject : blenderProjectDatabaseService.getRemainingQueueProjects()) {
                int throttle;
                List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
                int size = blenderFramePartList.size() - (blenderProject.getQueueIndex());
                if (size <= QUEUE) {
                    throttle = size;
                } else {
                    throttle = QUEUE - renderQueueDatabaseService.listPendingRender().size();
                }
                int queueIndex = blenderProject.getQueueIndex();
                addRenderQueueItem(blenderProject, throttle, queueIndex, blenderFramePartList);
            }
        }
    }

    private void addRenderQueueItem(BlenderProject blenderProject, int throttle, int queueIndex, List<BlenderFramePart> blenderFramePartList) {
        blenderProject = blenderProjectDatabaseService.getById(blenderProject.getId());
        if (!blenderProject.isQueueFillComplete()) {
            int index = queueIndex;
            int finalIndex = blenderProject.getTotalQueueSize() - 1;
            for (int i = 0; i <= throttle; i++) {
                if (renderQueueDatabaseService.listPendingRender().size() > QUEUE) {
                    break;
                }
                if (!renderQueueDatabaseService.checkExistingProjectIndex(blenderProject.getProject_uuid(), index)) {
                    LOG.debug("Adding to Queue item at index: " + index);
                    RenderQueueItem renderQueueItem = new RenderQueueItem();
                    renderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
                    renderQueueItem.setProjectIndex(index);
                    renderQueueItem.setProjectName(blenderProject.getProjectName());
                    renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                    renderQueueItem.setQueueItem_uuid(UUID.randomUUID().toString());
                    renderQueueItem.setComplete(false);
                    renderQueueItem.setPaused(false);
                    renderQueueItem.setConnection_uuid(null);
                    renderQueueItem.setBlenderFramePart(blenderFramePartList.get(index));
                    renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                } else {
                    if (blenderProject.getTotalQueueSize() > 1) {
                        LOG.debug("Queue already contains item with index for project");
                    }
                }

                if (index != finalIndex) {
                    index++;
                } else {
                    if (blenderProject.getTotalQueueSize() > 1) {
                        LOG.debug("Final index: " + finalIndex);
                    }
                }
            }
            blenderProject.setQueueIndex(index);
            if (index == finalIndex) {
                blenderProject.setQueueFillComplete(true);
            }
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
        }
    }

    @Override
    public void pauseBlenderProjectQueue(BlenderProject blenderProject) {
        queueActionItemList.add(new QueueActionItem(blenderProject, QueueAction.PAUSE));
    }

    @Override
    public void resumeBlenderProjectQueue(BlenderProject blenderProject) {
        queueActionItemList.add(new QueueActionItem(blenderProject, QueueAction.RESUME));
    }

    @Override
    public void stopBlenderProjectQueue(BlenderProject blenderProject) {
        queueActionItemList.add(new QueueActionItem(blenderProject, QueueAction.STOP));
    }

    private void processReceivedFiles() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<ProcessQueueItem> processQueueItemList = processQueueDatabaseService.listAll();
            if (!processQueueItemList.isEmpty()) {

                for (ProcessQueueItem processQueueItem : new ArrayList<>(processQueueItemList)) {
                    try {

                        processReceivedFile(processQueueItem, renderQueueDatabaseService,
                                blenderProjectDatabaseService, sethlansNodeDatabaseService,
                                processFrameDatabaseService, processQueueDatabaseService);
                    } catch (NullPointerException e) {
                        LOG.error("Received item after project has been stopped");
                        LOG.error(Throwables.getStackTraceAsString(e));
                        processQueueDatabaseService.delete(processQueueItem);
                    }
                }

            }
            modifyingQueue = false;
        }
    }

    private void processImages() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (processFrameDatabaseService.listAll().size() > 0) {
                for (ProcessFrameItem processFrameItem : processFrameDatabaseService.listAll()) {
                    BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(processFrameItem.getProjectUUID());
                    processImageAndAnimationService.combineParts(blenderProject, processFrameItem.getFrameNumber());
                    processFrameDatabaseService.delete(processFrameItem);
                }
            }
            modifyingQueue = false;
        }
    }

    private void finishProject() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            completeProcessing(blenderProjectDatabaseService, processImageAndAnimationService, renderQueueDatabaseService);
            modifyingQueue = false;
        }
    }


    private void assignQueueItemToNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            assignToNode(renderQueueDatabaseService, sethlansNodeDatabaseService);
            modifyingQueue = false;
        }
    }

    private void sendQueueItemsToAssignedNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            sendQueueItemsToNodes(renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNodeDatabaseService, sethlansAPIConnectionService);
            modifyingQueue = false;
        }
    }

    @Autowired
    public void setRenderQueueDatabaseService(RenderQueueDatabaseService renderQueueDatabaseService) {
        this.renderQueueDatabaseService = renderQueueDatabaseService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setProcessQueueDatabaseService(ProcessQueueDatabaseService processQueueDatabaseService) {
        this.processQueueDatabaseService = processQueueDatabaseService;
    }

    @Autowired
    public void setProcessImageAndAnimationService(ProcessImageAndAnimationService processImageAndAnimationService) {
        this.processImageAndAnimationService = processImageAndAnimationService;
    }

    @Autowired
    public void setProcessFrameDatabaseService(ProcessFrameDatabaseService processFrameDatabaseService) {
        this.processFrameDatabaseService = processFrameDatabaseService;
    }
}