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
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessFrameItem;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.database.*;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created Mario Estrella on 5/11/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class QueueProcessActions {
    private static final Logger LOG = LoggerFactory.getLogger(QueueProcessActions.class);

    static void processReceivedFile(ProcessQueueItem processQueueItem,
                                    RenderQueueDatabaseService renderQueueDatabaseService,
                                    BlenderProjectDatabaseService blenderProjectDatabaseService,
                                    SethlansNodeDatabaseService sethlansNodeDatabaseService,
                                    ProcessFrameDatabaseService processFrameDatabaseService,
                                    ProcessQueueDatabaseService processQueueDatabaseService) {
        RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processQueueItem.getQueueUUID());
        int partNumber = renderQueueItem.getBlenderFramePart().getPartNumber();
        int frameNumber = renderQueueItem.getBlenderFramePart().getFrameNumber();
        BlenderProject blenderProject =
                blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
        int remainingPartsForFrame = blenderProject.getPartsPerFrame();
        int remainingTotalQueue = 0;


        File storedDir = new File(renderQueueItem.getBlenderFramePart().getStoredDir());
        for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
            if (blenderFramePart.getFrameNumber() == frameNumber) {
                blenderFramePart.setStoredDir(storedDir.toString() + File.separator);
                if (blenderFramePart.getPartNumber() == partNumber) {
                    blenderFramePart.setProcessed(true);
                }
                if (blenderFramePart.isProcessed()) {
                    remainingPartsForFrame--;
                }
            }
            if (!blenderFramePart.isProcessed()) {
                remainingTotalQueue++;
            }
        }
        storedDir.mkdirs();
        try {
            LOG.debug("Processing received file from " +
                    sethlansNodeDatabaseService.getByConnectionUUID(processQueueItem.getConnection_uuid()).getHostname() + ". Frame: " + frameNumber +
                    " Part: " + partNumber
            );
            byte[] bytes = processQueueItem.getPart().getBytes(1, (int) processQueueItem.getPart().length());
            Path path = Paths.get(storedDir.toString() + File.separator +
                    renderQueueItem.getBlenderFramePart().getPartFilename() + "." +
                    renderQueueItem.getBlenderFramePart().getFileExtension());
            Files.write(path, bytes);
        } catch (IOException | SQLException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        renderQueueItem = renderQueueDatabaseService.getById(renderQueueItem.getId());
        renderQueueItem.setComplete(true);
        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
        LOG.debug("Processing complete.");
        int projectTotalQueue =
                blenderProject.getTotalQueueSize();

        LOG.debug("Remaining parts per frame for Frame " + frameNumber + ": " + remainingPartsForFrame);
        LOG.debug("Remaining items in project Queue " + blenderProject.getRemainingQueueSize());
        LOG.debug("Project total Queue " + blenderProject.getTotalQueueSize());

        double currentPercentage = ((projectTotalQueue - remainingTotalQueue) * 100.0) / projectTotalQueue;
        LOG.debug("Current Percentage " + currentPercentage);
        blenderProject.setCurrentPercentage((int) currentPercentage);

        blenderProject.setTotalRenderTime(blenderProject.getTotalRenderTime() + processQueueItem.getRenderTime());
        if (remainingTotalQueue > 0 && !blenderProject.getProjectStatus().equals(ProjectStatus.Paused)) {
            blenderProject.setProjectStatus(ProjectStatus.Rendering);
            blenderProject.setProjectEnd(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
        }
        if (remainingPartsForFrame == 0) {
            ProcessFrameItem processFrameItem = new ProcessFrameItem();
            processFrameItem.setProjectUUID(blenderProject.getProject_uuid());
            processFrameItem.setFrameNumber(frameNumber);
            processFrameDatabaseService.saveOrUpdate(processFrameItem);
        }
        blenderProject.setRemainingQueueSize(remainingTotalQueue);
        blenderProject.setTotalProjectTime(blenderProject.getProjectEnd() - blenderProject.getProjectStart());
        blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
        processQueueDatabaseService.delete(processQueueItem);
    }

    static void processIncoming(List<ProcessQueueItem> itemsReviewed, ProcessQueueItem processQueueItem, ProcessQueueDatabaseService processQueueDatabaseService, RenderQueueDatabaseService renderQueueDatabaseService, SethlansNodeDatabaseService sethlansNodeDatabaseService, BlenderProjectDatabaseService blenderProjectDatabaseService) {
        try {
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
                    if (sethlansNode.isCombined()) {
                        sethlansNode.setAllGPUSlotInUse(false);
                    } else {
                        for (GPUDevice gpuDevice : sethlansNode.getSelectedGPUs()) {
                            if (gpuDevice.getDeviceID().equals(renderQueueItem.getGpu_device_id())) {
                                gpuDevice.setInUse(false);
                            }
                        }
                    }
                    break;
                case CPU:
                    sethlansNode.setCpuSlotInUse(false);
                    break;
                default:
                    LOG.error("Invalid compute type, this message should not occur.");
            }
            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
            if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
            }
            if (sethlansNode.getAvailableRenderingSlots() == sethlansNode.getTotalRenderingSlots()) {
                sethlansNode.setAllGPUSlotInUse(false);
                sethlansNode.setCpuSlotInUse(false);
            }
            LOG.debug(sethlansNode.getHostname() + " state: " + sethlansNode.toString());
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            itemsReviewed.add(processQueueItem);
        } catch (NullPointerException e) {
            LOG.error("Received incoming items for queue after queue has been stopped");
            LOG.debug(Throwables.getStackTraceAsString(e));
            itemsReviewed.add(processQueueItem);
        }
    }

    static void completeProcessing(BlenderProjectDatabaseService blenderProjectDatabaseService,
                                   ProcessImageAndAnimationService processImageAndAnimationService,
                                   RenderQueueDatabaseService renderQueueDatabaseService,
                                   FrameFileUpdateDatabaseService frameFileUpdateDatabaseService,
                                   ProcessFrameDatabaseService processFrameDatabaseService) {
        for (BlenderProject blenderProject : blenderProjectDatabaseService.listAll()) {

            if (blenderProject.getProjectStatus().equals(ProjectStatus.Rendering) || blenderProject.getProjectStatus().equals(ProjectStatus.Started)) {
                if (frameFileUpdateDatabaseService.listByProjectUUID(blenderProject.getProject_uuid()).size() == 0
                        && processFrameDatabaseService.listbyProjectUUID(blenderProject.getProject_uuid()).size() == 0) {
                    if (blenderProject.getRemainingQueueSize() == 0) {
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
        }
    }
}
