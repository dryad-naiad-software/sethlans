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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.queue.NodeOnlineItem;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessIdleNode;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 5/20/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class QueueNodeStatusActions {
    private static final Logger LOG = LoggerFactory.getLogger(QueueNodeStatusActions.class);

    static void processOfflineNodes(SethlansNodeDatabaseService sethlansNodeDatabaseService,
                                    NodeOnlineItem nodeOnlineItem, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService,
                                    List<NodeOnlineItem> processedStatusNodes) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(nodeOnlineItem.getConnection_uuid());
        if (!nodeOnlineItem.isOnline()) {
            LOG.debug("Marking node " + sethlansNode.getHostname() + " as inactive.");
            switch (sethlansNode.getComputeType()) {
                case GPU:
                    sethlansNode.setAllGPUSlotInUse(false);
                    if (sethlansNode.isCombined()) {
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    } else {
                        if (sethlansNode.getAvailableRenderingSlots() == 1) {
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                        } else {
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
                        }
                    }
                    sethlansNode.setActive(false);
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
                case CPU:
                    sethlansNode.setActive(false);
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
                case CPU_GPU:
                    sethlansNode.setActive(false);
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setAllGPUSlotInUse(false);
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
            }
            if (renderQueueDatabaseService.listAll().size() > 0) {
                removeNodeFromQueue(sethlansNode.getConnection_uuid(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
            }
        }
        if (nodeOnlineItem.isOnline()) {
            LOG.debug("Marking node " + sethlansNode.getHostname() + " as active.");
            sethlansNode.setActive(true);
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        }

        processedStatusNodes.add(nodeOnlineItem);
    }

    static void processIdleNodes(SethlansNodeDatabaseService sethlansNodeDatabaseService,
                                 ProcessIdleNode idleNode, RenderQueueDatabaseService renderQueueDatabaseService,
                                 BlenderProjectDatabaseService blenderProjectDatabaseService, List<ProcessIdleNode> processedNodes, List<ProcessQueueItem> incomingQueueItemList) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(idleNode.getConnectionUUID());
        boolean waitingTobeProcessed = false;
        // Verify that the current node isn't waiting to be processed.
        for (ProcessQueueItem processQueueItem : incomingQueueItemList) {
            if (processQueueItem.getConnection_uuid().equals(idleNode.getConnectionUUID())) {
                waitingTobeProcessed = true;
            }
        }
        if (sethlansNode.isBenchmarkComplete() && !waitingTobeProcessed) {
            LOG.debug("Received idle notification from  " + sethlansNode.getHostname());
            switch (idleNode.getComputeType()) {
                case GPU:
                    sethlansNode.setAllGPUSlotInUse(false);
                    if (sethlansNode.isCombined()) {
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    } else {
                        if (sethlansNode.getAvailableRenderingSlots() == 1) {
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                        } else {
                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
                        }
                    }
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
                case CPU:
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
                case CPU_GPU:
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setAllGPUSlotInUse(false);
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    break;
            }

            removeNodeFromQueue(idleNode.getConnectionUUID(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
        }
        processedNodes.add(idleNode);
    }

    static void deleteNodes(Long nodeID, SethlansNodeDatabaseService sethlansNodeDatabaseService, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(nodeID);
        removeNodeFromQueue(sethlansNode.getConnection_uuid(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
        sethlansNodeDatabaseService.delete(nodeID);
    }

    static void disableNodes(Long nodeID, SethlansNodeDatabaseService sethlansNodeDatabaseService, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(nodeID);
        removeNodeFromQueue(sethlansNode.getConnection_uuid(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
        sethlansNode.setDisabled(true);
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
    }

    private static void removeNodeFromQueue(String connection_uuid, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService, SethlansNode sethlansNode) {
        List<RenderQueueItem> listOfItemsWIthNode = renderQueueDatabaseService.listQueueItemsByConnectionUUID(connection_uuid);
        List<RenderQueueItem> queueItemsNotComplete = new ArrayList<>();
        for (RenderQueueItem renderQueueItem : listOfItemsWIthNode) {
            if (!renderQueueItem.isComplete()) {
                queueItemsNotComplete.add(renderQueueItem);
            }
        }
        LOG.debug("Number of queue items assigned to " + sethlansNode.getHostname() + ": " + queueItemsNotComplete.size());
        for (RenderQueueItem renderQueueItem : queueItemsNotComplete) {
            renderQueueItem.setConnection_uuid(null);
            renderQueueItem.setRendering(false);
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
            renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
            renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
        }
    }
}
