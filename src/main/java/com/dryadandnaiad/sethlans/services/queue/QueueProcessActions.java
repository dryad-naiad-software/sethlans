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
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessFrameItem;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
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
        blenderProject.setRemainingQueueSize(blenderProject.getRemainingQueueSize() - 1);
        int remainingPartsForFrame = blenderProject.getPartsPerFrame();


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
            Thread.sleep(5000);
        } catch (IOException | SQLException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (InterruptedException e) {
            LOG.debug("Queue Process service shutting down");
        }
        renderQueueItem = renderQueueDatabaseService.getById(renderQueueItem.getId());
        renderQueueItem.setComplete(true);
        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
        LOG.debug("Processing complete.");
        int projectTotalQueue =
                blenderProject.getTotalQueueSize();
        int remainingTotalQueue = blenderProject.getRemainingQueueSize();

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
        blenderProject.setTotalProjectTime(blenderProject.getProjectEnd() - blenderProject.getProjectStart());
        blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
        processQueueDatabaseService.delete(processQueueItem);
    }
}
