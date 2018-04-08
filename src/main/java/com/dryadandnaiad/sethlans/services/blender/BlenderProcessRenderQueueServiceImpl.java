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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.database.BlenderProcessQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

/**
 * Created Mario Estrella on 3/30/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderProcessRenderQueueServiceImpl implements BlenderProcessRenderQueueService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProcessRenderQueueServiceImpl.class);
    private BlenderProcessQueueDatabaseService blenderProcessQueueDatabaseService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderProjectService blenderProjectService;
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private boolean populatingQueue;

    @Override
    public void addQueueItem(BlenderProcessQueueItem blenderProcessQueueItem) {
        populatingQueue = true;
        String hostname = sethlansNodeDatabaseService.getByConnectionUUID(blenderProcessQueueItem.getConnection_uuid()).getHostname();
        LOG.debug("Completed Render Task received from " + hostname + ". Adding to processing queue.");
        blenderProcessQueueDatabaseService.saveOrUpdate(blenderProcessQueueItem);
        populatingQueue = false;
    }

    @Override
    @Async
    public void startQueue() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOG.debug("Stopping Render Queue Service");
        }
        while (true) {
            try {
                Thread.sleep(5000);
                if (!populatingQueue) {
                    List<BlenderProcessQueueItem> blenderProcessQueueItemList = blenderProcessQueueDatabaseService.listAll();
                    if (!blenderProcessQueueItemList.isEmpty()) {
                        BlenderProcessQueueItem blenderProcessQueueItem = blenderProcessQueueItemList.get(0);
                        File storedDir = null;
                        BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderProcessQueueItem.getProject_uuid());
                        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.queueItemsByProjectUUID(blenderProject.getProject_uuid());

                        int projectTotalQueue = blenderProject.getPartsPerFrame() * blenderProject.getTotalNumOfFrames();
                        int remainingTotalQueue = projectTotalQueue;
                        int remainingPartsForFrame = 0;

                        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                            if (blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() == blenderProcessQueueItem.getFrame_number()
                                    && blenderRenderQueueItem.getBlenderFramePart().getPartNumber() == blenderProcessQueueItem.getPart_number()) {
                                blenderRenderQueueItem.setRendering(false);
                                blenderRenderQueueItem.setComplete(true);
                                blenderRenderQueueItem.setPaused(false);
                                blenderRenderQueueItem.getBlenderFramePart().setStoredDir(blenderProject.getProjectRootDir() +
                                        File.separator + "frame_" + blenderProcessQueueItem.getFrame_number() + File.separator);

                                storedDir = new File(blenderRenderQueueItem.getBlenderFramePart().getStoredDir());
                                storedDir.mkdirs();

                                try {
                                    // Save sent file
                                    byte[] bytes = blenderProcessQueueItem.getPart().getBytes(1, (int) blenderProcessQueueItem.getPart().length());
                                    Path path = Paths.get(storedDir.toString() + File.separator +
                                            blenderRenderQueueItem.getBlenderFramePart().getPartFilename() + "." +
                                            blenderRenderQueueItem.getBlenderFramePart().getFileExtension());
                                    Files.write(path, bytes);
                                    SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderProcessQueueItem.getConnection_uuid());
                                    switch (blenderRenderQueueItem.getRenderComputeType()) {
                                        case CPU:
                                            sethlansNode.setCpuSlotInUse(false);
                                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
                                            if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                                                sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                                            }
                                            break;
                                        case GPU:
                                            sethlansNode.setGpuSlotInUse(false);
                                            sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
                                            if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                                                sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                                            }
                                            break;
                                        default:
                                            LOG.debug("Invalid compute type specified for rendering.");
                                    }
                                    LOG.debug(sethlansNode.getHostname() + " has " + sethlansNode.getAvailableRenderingSlots() + " available rendering slot(s)");

                                    LOG.debug("Processing completed render from " + sethlansNode.getHostname() + ". Part: " + blenderProcessQueueItem.getPart_number()
                                            + " Frame: " + blenderProcessQueueItem.getFrame_number());
                                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);

                                } catch (IOException | SQLException e) {
                                    e.printStackTrace();
                                }
                                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                            }

                            if (blenderRenderQueueItem.isComplete()) {
                                remainingTotalQueue--;
                            }
                            if (!blenderRenderQueueItem.isComplete() && blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() == blenderProcessQueueItem.getFrame_number()) {
                                remainingPartsForFrame++;
                            }
                        }

                        for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
                            if (blenderFramePart.getFrameNumber() == blenderProcessQueueItem.getFrame_number()) {
                                blenderFramePart.setStoredDir(storedDir.toString() + File.separator);
                            }
                        }
                        LOG.debug("Remaining Parts per Frame for Frame " + blenderProcessQueueItem.getFrame_number() + ": " + remainingPartsForFrame + " out of " + blenderProject.getPartsPerFrame());
                        LOG.debug("Remaining Items in Queue: " + remainingTotalQueue);
                        LOG.debug("Project Total Queue " + projectTotalQueue);

                        double currentPercentage = ((projectTotalQueue - remainingTotalQueue) * 100.0) / projectTotalQueue;

                        LOG.debug("Current Percentage " + currentPercentage);

                        blenderProject.setCurrentPercentage((int) currentPercentage);

                        if (remainingTotalQueue > 0) {
                            blenderProject.setProjectStatus(ProjectStatus.Rendering);
                        }
                        if (remainingPartsForFrame == 0) {
                            if (blenderProjectService.combineParts(blenderProject, blenderProcessQueueItem.getFrame_number())) {
                                if (remainingTotalQueue == 0) {
                                    if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.AVI) {
                                        blenderProject.setProjectStatus(ProjectStatus.Processing);
                                        blenderProjectService.createAVI(blenderProject);
                                    }
                                    if (blenderProject.getProjectType() == ProjectType.ANIMATION && blenderProject.getRenderOutputFormat() == RenderOutputFormat.MP4) {
                                        blenderProject.setProjectStatus(ProjectStatus.Processing);
                                        blenderProjectService.createMP4(blenderProject);
                                    } else {
                                        blenderProject.setProjectStatus(ProjectStatus.Finished);
                                    }
                                    blenderProject.setAllImagesProcessed(true);
                                }
                            }

                        }
                        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                        blenderProcessQueueDatabaseService.delete(blenderProcessQueueItem);
                    }

                }
            } catch (InterruptedException e) {
                LOG.debug("Stopping Render Queue Service");
                break;
            }

        }

    }

    @Autowired
    public void setBlenderProcessQueueDatabaseService(BlenderProcessQueueDatabaseService blenderProcessQueueDatabaseService) {
        this.blenderProcessQueueDatabaseService = blenderProcessQueueDatabaseService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderRenderQueueDatabaseService(BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService) {
        this.blenderRenderQueueDatabaseService = blenderRenderQueueDatabaseService;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }
}
