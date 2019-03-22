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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.queue.*;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
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
                                    NodeOnlineItem nodeOnlineItem, RenderQueueDatabaseService renderQueueDatabaseService,
                                    BlenderProjectDatabaseService blenderProjectDatabaseService,
                                    SethlansNotificationService sethlansNotificationService,
                                    List<NodeOnlineItem> processedStatusNodes) {
        try {
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(nodeOnlineItem.getConnectionUUID());
            if (!nodeOnlineItem.isOnline()) {
                LOG.info("Marking node " + sethlansNode.getHostname() + " as inactive.");
                String message = sethlansNode.getHostname() + " has gone offline";
                SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SYSTEM, message);
                sethlansNotification.setMailable(true);
                sethlansNotification.setSubject("Node has gone offline!");
                sethlansNotification.setLinkPresent(true);
                sethlansNotification.setMessageLink("/admin/nodes");
                removeNodeFromQueue(sethlansNode.getConnectionUUID(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
                SethlansNodeUtils.resetNode(sethlansNode.getComputeType(), sethlansNode, true);
                sethlansNode.setActive(false);
                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);

                sethlansNotificationService.sendNotification(sethlansNotification);
            }
            if (nodeOnlineItem.isOnline()) {
                LOG.info("Marking node " + sethlansNode.getHostname() + " as active.");
                String message = sethlansNode.getHostname() + " is back online";
                SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SYSTEM, message);
                sethlansNotification.setMailable(true);
                sethlansNotification.setSubject("Node is back online!");
                sethlansNotification.setLinkPresent(true);
                sethlansNotification.setMessageLink("/admin/nodes");
                sethlansNotificationService.sendNotification(sethlansNotification);
                sethlansNode.setActive(true);
                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            }

            processedStatusNodes.add(nodeOnlineItem);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    static void processIdleNodes(SethlansNodeDatabaseService sethlansNodeDatabaseService,
                                 ProcessIdleNode idleNode, RenderQueueDatabaseService renderQueueDatabaseService,
                                 BlenderProjectDatabaseService blenderProjectDatabaseService, List<ProcessIdleNode> processedNodes, List<ProcessQueueItem> incomingQueueItemList) {
        try {
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(idleNode.getConnectionUUID());
            boolean waitingTobeProcessed = false;
            // Verify that the current node isn't waiting to be processed.
            for (ProcessQueueItem processQueueItem : incomingQueueItemList) {
                if (processQueueItem.getConnectionUUID().equals(idleNode.getConnectionUUID())) {
                    waitingTobeProcessed = true;
                }
            }
            if (sethlansNode.isBenchmarkComplete() && !waitingTobeProcessed) {
                LOG.debug("Received idle notification from  " + sethlansNode.getHostname());
                SethlansNodeUtils.resetNode(idleNode.getComputeType(), sethlansNode, false);
                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                LOG.debug(sethlansNode.toString());
                removeNodeFromQueue(idleNode.getConnectionUUID(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
            }
            processedNodes.add(idleNode);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    static void deleteNodes(Long nodeID, SethlansNodeDatabaseService sethlansNodeDatabaseService, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(nodeID);
        removeNodeFromQueue(sethlansNode.getConnectionUUID(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
        sethlansNodeDatabaseService.delete(nodeID);
    }

    static void disableNodes(Long nodeID, SethlansNodeDatabaseService sethlansNodeDatabaseService, RenderQueueDatabaseService renderQueueDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(nodeID);
        removeNodeFromQueue(sethlansNode.getConnectionUUID(), renderQueueDatabaseService, blenderProjectDatabaseService, sethlansNode);
        SethlansNodeUtils.resetNode(sethlansNode.getComputeType(), sethlansNode, true);
        sethlansNode.setDisabled(true);
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
    }

    static void updateSlots(NodeSlotUpdateItem updateItem, SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(updateItem.getConnection_uuid());
        sethlansNode.setAvailableRenderingSlots(updateItem.getAvailable_slots());
        if (updateItem.getDevice_id().equals("CPU")) {
            sethlansNode.setCpuSlotInUse(false);
        }
        if (updateItem.getDevice_id().equals("COMBO")) {
            sethlansNode.setAllGPUSlotInUse(false);
        }
        if (!updateItem.getDevice_id().equals("CPU") || !updateItem.getDevice_id().equals("COMBO")) {
            sethlansNode.setAllGPUSlotInUse(false);
            for (GPUDevice selectedGPUs : sethlansNode.getSelectedGPUs()) {
                if (updateItem.getDevice_id().equals(selectedGPUs.getDeviceID())) {
                    selectedGPUs.setInUse(false);
                }
            }
        }
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
            renderQueueItem.setConnectionUUID(null);
            renderQueueItem.setRendering(false);
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProjectUUID());
            renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
            renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
        }
    }
}
