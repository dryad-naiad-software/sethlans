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
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.queue.*;
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.services.database.*;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.dryadandnaiad.sethlans.services.queue.QueueNodeActions.*;
import static com.dryadandnaiad.sethlans.services.queue.QueueProcessActions.processReceivedFile;
import static com.dryadandnaiad.sethlans.services.queue.QueueProjectActions.queueProjectActions;

/**
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

    @Async
    @Override
    public void startQueue() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOG.debug("Stopping Blender Queue Service");
        }
        while (true) {
            try {
                Thread.sleep(1000);
                freeIdleNode();
                projectActions();
                processNodeAcknowledgements();
                projectActions();
                assignQueueItemToNode();
                projectActions();
                sendQueueItemsToAssignedNode();
                projectActions();
                processNodeAcknowledgements();
                projectActions();
                processReceivedFiles();
                projectActions();
                processImages();
                projectActions();
                finishProject();
            } catch (InterruptedException e) {
                LOG.debug("Stopping Blender Queue Service");
                break;
            }
        }
    }

    @Override
    public void queueIdleNode(String connection_uuid, ComputeType computeType) {
        idleNodes.add(new ProcessIdleNode(connection_uuid, computeType));
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
    public boolean addItemToProcess(ProcessQueueItem processQueueItem) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            processQueueDatabaseService.saveOrUpdate(processQueueItem);
            RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processQueueItem.getQueueUUID());
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(processQueueItem.getConnection_uuid());
            ComputeType computeType = renderQueueItem.getRenderComputeType();
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
            renderQueueItem.getBlenderFramePart().setStoredDir(blenderProject.getProjectRootDir() +
                    File.separator + "frame_" + renderQueueItem.getBlenderFramePart().getFrameNumber() + File.separator);
            renderQueueDatabaseService.saveOrUpdate(renderQueueItem);

            LOG.debug("Completed Render Task received from " + sethlansNode.getHostname() + ". Adding to processing queue.");

            switch (computeType) {
                case GPU:
                    sethlansNode.setGpuSlotInUse(false);
                    break;
                case CPU:
                    sethlansNode.setCpuSlotInUse(false);
                    break;
                default:
                    LOG.error("Invalid compute type, this message should not occur.");
            }
            sethlansNode.setAvailableRenderingSlots(Math.max(sethlansNode.getTotalRenderingSlots(), sethlansNode.getAvailableRenderingSlots() + 1));
            if (sethlansNode.getAvailableRenderingSlots() == sethlansNode.getTotalRenderingSlots()) {
                sethlansNode.setGpuSlotInUse(false);
                sethlansNode.setCpuSlotInUse(false);
            }
            LOG.debug(sethlansNode.getHostname() + " state: " + sethlansNode.toString());
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            modifyingQueue = false;
            return true;
        }
        return false;
    }

    private void projectActions() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (queueActionItemList.size() > 0) {
                List<QueueActionItem> processedAction = new ArrayList<>();
                for (QueueActionItem queueActionItem : queueActionItemList) {
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
                for (ProcessIdleNode idleNode : idleNodes) {
                    processIdleNodes(sethlansNodeDatabaseService, idleNode,
                            renderQueueDatabaseService, blenderProjectDatabaseService, processedNodes);
                }
                idleNodes.removeAll(processedNodes);
            }
            modifyingQueue = false;
        }
    }

    private void processNodeAcknowledgements() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            if (nodeStatuses.size() > 0) {
                List<ProcessNodeStatus> itemsProcessed = new ArrayList<>();
                for (ProcessNodeStatus processNodeStatus : nodeStatuses) {
                    processAcknowledgements(processNodeStatus, renderQueueDatabaseService,
                            blenderProjectDatabaseService, sethlansNodeDatabaseService, itemsProcessed);
                }
                nodeStatuses.removeAll(itemsProcessed);
            }
            modifyingQueue = false;
        }
    }

    @Override
    public boolean populateQueueWithProject(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
            for (BlenderFramePart blenderFramePart : blenderFramePartList) {
                RenderQueueItem renderQueueItem = new RenderQueueItem();
                renderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
                renderQueueItem.setProjectName(blenderProject.getProjectName());
                renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                renderQueueItem.setQueueItem_uuid(UUID.randomUUID().toString());
                renderQueueItem.setComplete(false);
                renderQueueItem.setPaused(false);
                renderQueueItem.setConnection_uuid(null);
                renderQueueItem.setBlenderFramePart(blenderFramePart);
                renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
            }
            modifyingQueue = false;
            LOG.debug("Render Queue configured, " + renderQueueDatabaseService.listPendingRender().size() + " items in queue");
            return true;
        }
        return false;
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
                LOG.debug("Running processing queue.");
                for (ProcessQueueItem processQueueItem : processQueueItemList) {
                    processReceivedFile(processQueueItem, renderQueueDatabaseService,
                            blenderProjectDatabaseService, sethlansNodeDatabaseService,
                            processFrameDatabaseService, processQueueDatabaseService);
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
            for (BlenderProject blenderProject : blenderProjectDatabaseService.listAll()) {
                if (blenderProject.getProjectStatus().equals(ProjectStatus.Rendering) || blenderProject.getProjectStatus().equals(ProjectStatus.Started)) {
                    if (renderQueueDatabaseService.listRemainingQueueItemsByProjectUUID(blenderProject.getProject_uuid()).size() == 0) {
                        if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.AVI) {
                            blenderProject.setProjectStatus(ProjectStatus.Processing);
                            blenderProject.setCurrentPercentage(100);
                            processImageAndAnimationService.createAVI(blenderProject);
                        }
                        if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.MP4) {
                            blenderProject.setProjectStatus(ProjectStatus.Processing);
                            blenderProject.setCurrentPercentage(100);
                            processImageAndAnimationService.createMP4(blenderProject);
                        } else {
                            blenderProject.setProjectStatus(ProjectStatus.Finished);
                            blenderProject.setCurrentPercentage(100);
                            blenderProject.setProjectEnd(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
                        }
                        blenderProject.setAllImagesProcessed(true);
                        blenderProject.setTotalProjectTime(blenderProject.getProjectEnd() - blenderProject.getProjectStart());
                        blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                        renderQueueDatabaseService.deleteAllByProject(blenderProject.getProject_uuid());
                    }
                }
            }
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