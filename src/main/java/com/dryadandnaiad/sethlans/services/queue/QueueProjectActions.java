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
import com.dryadandnaiad.sethlans.domains.database.queue.QueueActionItem;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.ProcessQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created Mario Estrella on 5/11/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class QueueProjectActions {
    private static final Logger LOG = LoggerFactory.getLogger(QueueProjectActions.class);

    static void queueProjectActions(QueueActionItem queueActionItem, RenderQueueDatabaseService renderQueueDatabaseService,
                                    BlenderProjectDatabaseService blenderProjectDatabaseService, ProcessQueueDatabaseService processQueueDatabaseService,
                                    SethlansNodeDatabaseService sethlansNodeDatabaseService, List<QueueActionItem> processedAction) {
        BlenderProject blenderProject = queueActionItem.getBlenderProject();
        List<RenderQueueItem> renderQueueItemList;
        switch (queueActionItem.getQueueAction()) {
            case PAUSE:
                LOG.debug("Pausing queue for " + blenderProject.getProjectName());
                renderQueueItemList =
                        renderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid());
                for (RenderQueueItem renderQueueItem : renderQueueItemList) {
                    if (!renderQueueItem.isComplete()) {
                        renderQueueItem.setPaused(true);
                        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                    }
                }
                blenderProject.setProjectStatus(ProjectStatus.Paused);
                blenderProject.setProjectEnd(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
                blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                break;
            case RESUME:
                LOG.debug("Resuming queue for " + blenderProject.getProjectName());
                renderQueueItemList =
                        renderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid());
                for (RenderQueueItem renderQueueItem : renderQueueItemList) {
                    if (!renderQueueItem.isComplete()) {
                        renderQueueItem.setPaused(false);
                        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                    }
                }
                blenderProject.setProjectStatus(ProjectStatus.Pending);
                blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                break;
            case STOP:
                LOG.debug("Stopping queue for " + blenderProject.getProjectName());
                for (RenderQueueItem renderQueueItem : renderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid())) {
                    if (processQueueDatabaseService.getListOfProcessByProject(blenderProject.getProject_uuid()).size() > 0) {
                        if (processQueueDatabaseService.getProcessByQueueItem(renderQueueItem.getQueueItem_uuid()) != null) {
                            processQueueDatabaseService.delete(processQueueDatabaseService.getProcessByQueueItem(renderQueueItem.getQueueItem_uuid()));
                        }
                    }
                    if (renderQueueItem.getConnection_uuid() != null) {
                        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(renderQueueItem.getConnection_uuid());
                        switch (renderQueueItem.getRenderComputeType()) {
                            case CPU:
                                sethlansNode.setCpuSlotInUse(false);
                                if (sethlansNode.getAvailableRenderingSlots() <= 0) {
                                    sethlansNode.setAvailableRenderingSlots(1);
                                } else {
                                    sethlansNode.setAvailableRenderingSlots(Math.max(sethlansNode.getTotalRenderingSlots(), sethlansNode.getAvailableRenderingSlots() + 1));
                                }
                                break;
                            case GPU:
                                sethlansNode.setAllGPUSlotInUse(false);
                                if (sethlansNode.getAvailableRenderingSlots() <= 0) {
                                    sethlansNode.setAvailableRenderingSlots(1);
                                } else {
                                    sethlansNode.setAvailableRenderingSlots(Math.max(sethlansNode.getTotalRenderingSlots(), sethlansNode.getAvailableRenderingSlots() + 1));
                                }
                                break;
                            default:
                                LOG.error("Wrong compute type used, this message should not be displayed.");
                        }
                        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                    }

                }
                renderQueueDatabaseService.deleteAllByProject(blenderProject.getProject_uuid());
                if (!blenderProject.isAllImagesProcessed() && blenderProjectDatabaseService.getById(blenderProject.getId()) != null) {
                    blenderProject.setProjectStatus(ProjectStatus.Added);
                    blenderProject.setTotalProjectTime(0L);
                    blenderProject.setProjectStart(0L);
                    blenderProject.setProjectEnd(0L);
                    blenderProject.setFrameFileNames(new ArrayList<>());
                    blenderProject.setCurrentFrameThumbnail(null);
                    blenderProject.setCurrentPercentage(0);
                    blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                    blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                }
                break;
        }
        processedAction.add(queueActionItem);
    }
}
