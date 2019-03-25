/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.queue.*;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.QueueAction;
import com.dryadandnaiad.sethlans.services.database.*;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
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
    private FrameFileUpdateDatabaseService frameFileUpdateDatabaseService;
    private SethlansNotificationService sethlansNotificationService;
    private GetRawDataService getRawDataService;
    private List<ProcessNodeStatus> nodeStatuses = new ArrayList<>();
    private List<ProcessIdleNode> idleNodes = new ArrayList<>();
    private List<QueueActionItem> queueActionItemList = new ArrayList<>();
    private List<ProcessQueueItem> incomingQueueItemList = new ArrayList<>();
    private List<NodeSlotUpdateItem> nodeSlotUpdateItemList = new ArrayList<>();
    private Set<NodeOnlineItem> nodeOnlineItemList = new HashSet<>();
    private List<Long> nodesToDelete = new ArrayList<>();
    private List<Long> nodesToDisable = new ArrayList<>();
    private final static int QUEUE = 500;
    private final static int CLEANUP = QUEUE / 4;

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
                // When server is idle no need to pool the queue often.
                if (renderQueueDatabaseService.listPendingRender().size() > 0) {
                    Thread.sleep(250);
                } else {
                    Thread.sleep(1000);
                }
                if (!queueActionItemList.isEmpty()) {
                    projectActions();
                    continue;
                }

                if (!nodeOnlineItemList.isEmpty()) {
                    nodeOnlineStatus();
                    continue;
                }

                if (!nodesToDelete.isEmpty()) {
                    processNodeDeletion();
                    continue;
                }

                if (!nodesToDisable.isEmpty()) {
                    processDisablingNodes();
                    continue;
                }

                if (!nodeSlotUpdateItemList.isEmpty()) {
                    processNodeSlotUpdates();
                    continue;
                }

                if (!idleNodes.isEmpty()) {
                    freeIdleNode();
                    continue;
                }

                if (incomingQueueItemList.isEmpty()) {
                    if (renderQueueDatabaseService.tableSize() > CLEANUP) {
                        cleanQueue();
                    }
                    if (sethlansNodeDatabaseService.activeNodeList().size() > 0 && blenderProjectDatabaseService.tableSize() > 0) {
                        populateQueue();
                    }
                    if (!processQueueDatabaseService.listAll().isEmpty()) {
                        processReceivedFiles();
                    }
                    assignmentWorkflow();
                    if (!nodeStatuses.isEmpty()) {
                        processNodeAcknowledgements();
                    }
                    processingWorkflow();
                } else {
                    incomingCompleteItems();
                    assignmentWorkflow();
                    if (!nodeStatuses.isEmpty()) {
                        processNodeAcknowledgements();
                    }
                    processingWorkflow();
                    if (!processQueueDatabaseService.listAll().isEmpty()) {
                        processReceivedFiles();
                    }
                }



            } catch (InterruptedException e) {
                LOG.debug("Stopping Sethlans Queue Service");
                break;
            }
        }
    }

    private void processNodeSlotUpdates() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodeSlotUpdateItemList.size() > 0) {
                List<NodeSlotUpdateItem> reviewedUpdates = new ArrayList<>();
                for (NodeSlotUpdateItem updateItem : new ArrayList<>(nodeSlotUpdateItemList)) {
                    updateSlots(updateItem, sethlansNodeDatabaseService);
                    reviewedUpdates.add(updateItem);
                }
                nodeSlotUpdateItemList.removeAll(reviewedUpdates);
            }
        }
        modifyingQueue = false;
    }

    private void assignmentWorkflow() throws InterruptedException {
        if (!nodeStatuses.isEmpty()) {
            processNodeAcknowledgements();
        }
        assignQueueItemToNode();
        sendQueueItemsToAssignedNode();
        Thread.sleep(5000);
    }

    private void processingWorkflow() {
        updateFrames();
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
    public void nodeSlotUpdate(String connection_uuid, String device_id, int available_slots) {
        nodeSlotUpdateItemList.add(new NodeSlotUpdateItem(connection_uuid, device_id, available_slots));
    }

    @Override
    public void addNodeToDeleteQueue(Long id) {
        nodesToDelete.add(id);
    }

    @Override
    public void nodeRejectQueueItem(String queue_uuid) {
        ProcessQueueItem queueItem = processQueueDatabaseService.getProcessByQueueUUID(queue_uuid);
        if (queueItem != null) {
            nodeStatuses.add(new ProcessNodeStatus(queue_uuid, false));
        } else {
            LOG.info("Rejection received for item that is no longer in queue.");
        }


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
            List<ProcessQueueItem> itemsReviewed = new ArrayList<>();
            for (ProcessQueueItem processQueueItem : new ArrayList<>(incomingQueueItemList)) {
                processIncoming(itemsReviewed, processQueueItem, processQueueDatabaseService, renderQueueDatabaseService, sethlansNodeDatabaseService,
                        blenderProjectDatabaseService, getRawDataService);
            }
            incomingQueueItemList.removeAll(itemsReviewed);
            modifyingQueue = false;
        }
    }


    private void projectActions() throws InterruptedException {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (queueActionItemList.size() > 0) {
                List<QueueActionItem> processedAction = new ArrayList<>();
                for (QueueActionItem queueActionItem : new ArrayList<>(queueActionItemList)) {
                    queueProjectActions(queueActionItem, renderQueueDatabaseService,
                            blenderProjectDatabaseService, processQueueDatabaseService,
                            sethlansNodeDatabaseService, processedAction, sethlansAPIConnectionService, getRawDataService);
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
                    processOfflineNodes(sethlansNodeDatabaseService, nodeOnlineItem, renderQueueDatabaseService,
                            blenderProjectDatabaseService, sethlansNotificationService, processedStatusNodes);
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
            for (RenderQueueItem renderQueueItem : renderQueueDatabaseService.listAll()) {
                if (renderQueueItem.isComplete()) {
                    renderQueueDatabaseService.delete(renderQueueItem);
                }
            }
            modifyingQueue = false;
        }
    }

    private void populateQueue() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            populateQueueRunningProjects();
            populateQueuePendingProjects();
            modifyingQueue = false;
        }
    }

    private void populateQueuePendingProjects() {
        if (renderQueueDatabaseService.listPendingRender().size() < QUEUE && blenderProjectDatabaseService.pendingProjectsSize() > 0) {
            for (BlenderProject blenderProject : blenderProjectDatabaseService.getPendingProjects()) {
                int throttle;
                List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
                if (blenderFramePartList.size() <= QUEUE) {
                    throttle = blenderFramePartList.size();
                } else {
                    throttle = QUEUE - renderQueueDatabaseService.listPendingRender().size();
                }
                addRenderQueueItem(blenderProject, throttle, blenderFramePartList);

            }
        }
    }

    private void populateQueueRunningProjects() {
        if (renderQueueDatabaseService.listPendingRender().size() <= CLEANUP && blenderProjectDatabaseService.remainingQueueProjectsSize() > 0) {
            for (BlenderProject blenderProject : blenderProjectDatabaseService.getRemainingQueueProjects()) {
                int throttle;
                List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
                int size = blenderFramePartList.size() - (blenderProject.getQueueIndex());
                if (size <= QUEUE) {
                    throttle = size;
                } else {
                    throttle = QUEUE - renderQueueDatabaseService.listPendingRender().size();
                }

                addRenderQueueItem(blenderProject, throttle, blenderFramePartList);
            }
        }
    }

    private void addRenderQueueItem(BlenderProject blenderProject, int throttle, List<BlenderFramePart> blenderFramePartList) {
        blenderProject = blenderProjectDatabaseService.getById(blenderProject.getId());
        if (!blenderProject.isQueueFillComplete()) {
            int index = blenderProject.getQueueIndex();
            int finalIndex = blenderProject.getTotalQueueSize() - 1;
            for (int i = 0; i <= throttle; i++) {
                if (renderQueueDatabaseService.listPendingRender().size() > QUEUE) {
                    break;
                }
                if (index <= finalIndex) {
                    if (!renderQueueDatabaseService.checkExistingProjectIndex(blenderProject.getProjectUUID(), index)) {
                        LOG.debug("Adding to Queue item at index: " + index);
                        RenderQueueItem renderQueueItem = new RenderQueueItem();
                        renderQueueItem.setProjectUUID(blenderProject.getProjectUUID());
                        renderQueueItem.setProjectIndex(index);
                        renderQueueItem.setProjectName(blenderProject.getProjectName());
                        renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                        renderQueueItem.setQueueItemUUID(UUID.randomUUID().toString());
                        renderQueueItem.setComplete(false);
                        renderQueueItem.setPaused(false);
                        renderQueueItem.setConnectionUUID(null);
                        renderQueueItem.setBlenderFramePart(blenderFramePartList.get(index));
                        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                    }
                }
                index++;
            }
            if (index > finalIndex) {
                blenderProject.setQueueFillComplete(true);
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            }
            if (index <= finalIndex) {
                blenderProject.setQueueIndex(index);
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            }
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
                BlenderProject blenderProject = null;
                for (int i = 0; i < processQueueItemList.size(); i++) {
                    ProcessQueueItem processQueueItem = processQueueItemList.get(i);
                    if (i == 0) {
                        blenderProject = blenderProjectDatabaseService.getByProjectUUID(processQueueItem.getProjectUUID());
                    }
                    if (i > 0) {
                        if (!processQueueItemList.get(i - 1).getProjectUUID().equals(processQueueItem.getProjectUUID())) {
                            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                            blenderProject = blenderProjectDatabaseService.getByProjectUUID(processQueueItemList.get(i).getProjectUUID());
                        }
                    }
                    RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processQueueItem.getQueueUUID());
                    startProcessingFiles(processQueueItemList.get(i), blenderProject, renderQueueItem);
                    if (renderQueueItem != null) {
                        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                    }
                }
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            }
            modifyingQueue = false;
        }
    }

    private void startProcessingFiles(ProcessQueueItem processQueueItem, BlenderProject blenderProject, RenderQueueItem renderQueueItem) {
        try {
            processReceivedFile(processQueueItem, renderQueueItem, blenderProject, sethlansNodeDatabaseService,
                    processFrameDatabaseService, processQueueDatabaseService);
        } catch (NullPointerException e) {
            LOG.error("Received item after project has been stopped");
            LOG.debug(Throwables.getStackTraceAsString(e));
            processQueueDatabaseService.delete(processQueueItem);
        }
    }

    private void processImages() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (processFrameDatabaseService.tableSize() > 0) {
                for (ProcessFrameItem processFrameItem : processFrameDatabaseService.listAll()) {
                    BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(processFrameItem.getProjectUUID());
                    List<Boolean> allPartsProcessed = new ArrayList<>();
                    for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
                        if (processFrameItem.getFrameNumber() == blenderFramePart.getFrameNumber()) {
                            allPartsProcessed.add(blenderFramePart.isProcessed());
                        }
                    }
                    if (!allPartsProcessed.contains(false)) {
                        processImageAndAnimationService.combineParts(blenderProject, processFrameItem.getFrameNumber());
                        processFrameDatabaseService.delete(processFrameItem);
                    }
                }
            }
            modifyingQueue = false;
        }
    }

    private void updateFrames() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (frameFileUpdateDatabaseService.tableSize() > 0) {
                LOG.debug("Updating Projects with frame file names");
                BlenderProject blenderProject = null;
                for (FrameFileUpdateItem frameFileUpdateItem : frameFileUpdateDatabaseService.listAll()) {
                    if (blenderProject == null) {
                        blenderProject = blenderProjectDatabaseService.getByProjectUUID(frameFileUpdateItem.getProjectUUID());
                    } else {
                        if (!blenderProject.getProjectUUID().equals(frameFileUpdateItem.getProjectUUID())) {
                            // If the frame update is for another project. Save the current project then start the update of the new project
                            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                            blenderProject = blenderProjectDatabaseService.getByProjectUUID(frameFileUpdateItem.getProjectUUID());
                        }
                    }
                    blenderProject.getFrameFileNames().add(frameFileUpdateItem.getFrameFileName());
                    blenderProject.setCurrentFrameThumbnail(frameFileUpdateItem.getCurrentFrameThumbnail());
                    blenderProject.setCompletedFrames(blenderProject.getCompletedFrames() + 1);
                    frameFileUpdateDatabaseService.delete(frameFileUpdateItem);
                }
                if (blenderProject != null && !blenderProject.isUserStopped()) {
                    blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                }
                LOG.debug("Completed File name update");
            }
            modifyingQueue = false;
        }
    }

    private void finishProject() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            completeProcessing(blenderProjectDatabaseService, processImageAndAnimationService, renderQueueDatabaseService,
                    frameFileUpdateDatabaseService, sethlansNotificationService, processFrameDatabaseService);
            modifyingQueue = false;
        }
    }


    private void assignQueueItemToNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            queueItemToNode(renderQueueDatabaseService, sethlansNodeDatabaseService, getRawDataService);
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

    @Autowired
    public void setFrameFileUpdateDatabaseService(FrameFileUpdateDatabaseService frameFileUpdateDatabaseService) {
        this.frameFileUpdateDatabaseService = frameFileUpdateDatabaseService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }

    @Autowired
    public void setGetRawDataService(GetRawDataService getRawDataService) {
        this.getRawDataService = getRawDataService;
    }
}