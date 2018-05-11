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
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.database.*;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                // TODO add node operations before assignments
                assignQueueItemToNode();
                sendQueueItemsToAssignedNode();
                processReceivedFiles();
                processImages();
                finishProject();
            } catch (InterruptedException e) {
                LOG.debug("Stopping Blender Queue Service");
                break;

            }

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
    public boolean pauseBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Pausing queue for " + blenderProject.getProjectName());
            List<RenderQueueItem> renderQueueItemList =
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
            modifyingQueue = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Resuming queue for " + blenderProject.getProjectName());
            List<RenderQueueItem> renderQueueItemList =
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
            modifyingQueue = false;
            return true;
        }
        return false;

    }

    @Override
    public boolean stopBlenderProjectQueue(BlenderProject blenderProject) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            LOG.debug("Stopping queue for " + blenderProject.getProjectName());
            for (RenderQueueItem renderQueueItem : renderQueueDatabaseService.listQueueItemsByProjectUUID(blenderProject.getProject_uuid())) {
                processQueueDatabaseService.delete(processQueueDatabaseService.getProcessByQueueItem(renderQueueItem.getQueueItem_uuid()));
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
                            sethlansNode.setGpuSlotInUse(false);
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
            if (!blenderProject.isAllImagesProcessed()) {
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
            modifyingQueue = false;
            return true;
        }
        return false;
    }

    @Override
    public void nodeRejectQueueItem(String queue_uuid) {
        //TODO create a db table to handle node operations
    }

    @Override
    public void nodeAcknowledgeQueueItem(String queue_uuid) {
        //TODO create a db table to handle node operations

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

            renderQueueItem.setRendering(false);
            renderQueueItem.setPaused(false);
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
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            modifyingQueue = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean nodeIdle(String connection_uuid, ComputeType computeType) {
        if (!modifyingQueue) {
            modifyingQueue = true;
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
            if (sethlansNode.isBenchmarkComplete()) {
                LOG.debug("Received idle notification from  " + sethlansNode.getHostname());
                switch (computeType) {
                    case GPU:
                        sethlansNode.setGpuSlotInUse(false);
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                        break;
                    case CPU:
                        sethlansNode.setCpuSlotInUse(false);
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                        break;
                    case CPU_GPU:
                        sethlansNode.setCpuSlotInUse(false);
                        sethlansNode.setGpuSlotInUse(false);
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                        break;
                }

                List<RenderQueueItem> listOfItemsWIthNode = renderQueueDatabaseService.listQueueItemsByConnectionUUID(connection_uuid);
                LOG.debug("List of queue items assigned to idle node " + listOfItemsWIthNode);
                for (RenderQueueItem renderQueueItem : listOfItemsWIthNode) {
                    if (!renderQueueItem.isComplete()) {
                        renderQueueItem.setConnection_uuid(null);
                        renderQueueItem.setRendering(false);
                        BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
                        renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
                    }
                }
            }
            modifyingQueue = false;
            return true;
        }
        return false;

    }

    private void processReceivedFiles() {
        if (!modifyingQueue) {

            modifyingQueue = true;
            List<ProcessQueueItem> processQueueItemList = processQueueDatabaseService.listAll();
            if (!processQueueItemList.isEmpty()) {
                LOG.debug("Running processing queue.");
                for (ProcessQueueItem processQueueItem : processQueueItemList) {
                    RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processQueueItem.getQueueUUID());
                    int partNumber = renderQueueItem.getBlenderFramePart().getPartNumber();
                    int frameNumber = renderQueueItem.getBlenderFramePart().getFrameNumber();
                    BlenderProject blenderProject =
                            blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
                    int projectTotalQueue =
                            renderQueueDatabaseService.listQueueItemsByProjectUUID(renderQueueItem.getProject_uuid()).size();
                    int remainingTotalQueue =
                            renderQueueDatabaseService.listRemainingQueueItemsByProjectUUID(renderQueueItem.getProject_uuid()).size();
                    int remainingPartsForFrame =
                            renderQueueDatabaseService.listRemainingPartsInProjectQueueByFrameNumber(
                                    renderQueueItem.getProject_uuid(), frameNumber).size();

                    File storedDir = new File(renderQueueItem.getBlenderFramePart().getStoredDir());
                    for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
                        if (blenderFramePart.getFrameNumber() == frameNumber) {
                            blenderFramePart.setStoredDir(storedDir.toString() + File.separator);
                        }
                    }
                    storedDir.mkdirs();
                    try {
                        // Save sent file
                        byte[] bytes = processQueueItem.getPart().getBytes(1, (int) processQueueItem.getPart().length());
                        Path path = Paths.get(storedDir.toString() + File.separator +
                                renderQueueItem.getBlenderFramePart().getPartFilename() + "." +
                                renderQueueItem.getBlenderFramePart().getFileExtension());
                        Files.write(path, bytes);

                        LOG.debug("Processing completed render from " +
                                sethlansNodeDatabaseService.getByConnectionUUID(processQueueItem.getConnection_uuid()).getHostname() +
                                ". Part: " + partNumber
                                + " Frame: " + frameNumber);
                        Thread.sleep(10000);


                    } catch (IOException | InterruptedException | SQLException e) {
                        LOG.error(Throwables.getStackTraceAsString(e));
                    }
                    LOG.debug("Remaining parts per frame for Frame " + frameNumber + ": " + remainingPartsForFrame);
                    LOG.debug("Remaining items in project Queue " + remainingTotalQueue);
                    LOG.debug("Project total Queue " + projectTotalQueue);

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

                    renderQueueItem = renderQueueDatabaseService.getById(renderQueueItem.getId());
                    renderQueueItem.setComplete(true);
                    renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                    blenderProject.setTotalProjectTime(blenderProject.getProjectEnd() - blenderProject.getProjectStart());
                    blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                    blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                    processQueueDatabaseService.delete(processQueueItem);
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
                    List<RenderQueueItem> toDelete = renderQueueDatabaseService.queueItemsByFrameNumber(processFrameItem.getProjectUUID(), processFrameItem.getFrameNumber());
                    for (RenderQueueItem renderQueueItem : toDelete) {
                        renderQueueDatabaseService.delete(renderQueueItem);
                    }
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
                    }
                }
            }
            modifyingQueue = false;
        }
    }

    private void assignQueueItemToNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<RenderQueueItem> renderQueueItemList = renderQueueDatabaseService.listPendingRender();
            int totalAvailableSlots = sethlansNodeDatabaseService.activeNodeswithFreeSlots().size();
            if (renderQueueItemList.size() > 0 && totalAvailableSlots > 0) {
                List<SethlansNode> sortedSethlansNodeList;
                int count;
                if (totalAvailableSlots > renderQueueItemList.size()) {
                    count = renderQueueItemList.size();
                } else {
                    count = totalAvailableSlots;
                }
                for (int i = 0; i < count; i++) {
                    RenderQueueItem renderQueueItem = renderQueueItemList.get(i);
                    LOG.debug(renderQueueItem.getProjectName() + " uuid: " +
                            renderQueueItem.getProject_uuid() + " Frame: "
                            + renderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
                            + renderQueueItem.getBlenderFramePart().getPartNumber() + " is waiting to be rendered.");
                    SethlansNode sethlansNode;
                    switch (renderQueueItem.getRenderComputeType()) {
                        case CPU_GPU:
                            sortedSethlansNodeList = getSortedNodeList(ComputeType.CPU_GPU);
                            if (sortedSethlansNodeList != null) {
                                sethlansNode = sortedSethlansNodeList.get(0);
                                renderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                                renderQueueItem = setQueueItemComputeType(sethlansNode, renderQueueItem);
                                if (renderQueueItem != null) {
                                    switch (renderQueueItem.getRenderComputeType()) {
                                        case CPU:
                                            sethlansNode.setCpuSlotInUse(true);
                                            break;
                                        case GPU:
                                            sethlansNode.setGpuSlotInUse(true);
                                            break;
                                        case CPU_GPU:
                                            LOG.error("Failure in logic this message should not be displayed.");
                                            break;
                                    }
                                    sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
                                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                                }
                            }
                            break;
                        case CPU:
                            sortedSethlansNodeList = getSortedNodeList(ComputeType.CPU);
                            if (sortedSethlansNodeList != null) {
                                sethlansNode = sortedSethlansNodeList.get(0);
                                renderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                                sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
                                sethlansNode.setCpuSlotInUse(true);
                                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            }
                            break;
                        case GPU:
                            sortedSethlansNodeList = getSortedNodeList(ComputeType.GPU);
                            if (sortedSethlansNodeList != null) {
                                sethlansNode = sortedSethlansNodeList.get(0);
                                renderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                                sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
                                sethlansNode.setGpuSlotInUse(true);
                                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            }
                            break;
                    }
                    renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
                }
            }
            modifyingQueue = false;
        }
    }

    private void sendQueueItemsToAssignedNode() {
        if (!modifyingQueue) {
            modifyingQueue = true;
            List<RenderQueueItem> renderQueueItemList = renderQueueDatabaseService.listPendingRenderWithNodeAssigned();
            for (RenderQueueItem renderQueueItem : renderQueueItemList) {
                modifyingQueue = true;
                BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProject_uuid());
                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(renderQueueItem.getConnection_uuid());
                String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" +
                        sethlansNode.getNetworkPort() + "/api/render/request";
                String params = "project_name=" + blenderProject.getProjectName() +
                        "&connection_uuid=" + sethlansNode.getConnection_uuid() +
                        "&project_uuid=" + blenderProject.getProject_uuid() +
                        "&queue_item_uuid=" + renderQueueItem.getQueueItem_uuid() +
                        "&render_output_format=" + blenderProject.getRenderOutputFormat() +
                        "&samples=" + blenderProject.getSamples() +
                        "&blender_engine=" + blenderProject.getBlenderEngine() +
                        "&compute_type=" + renderQueueItem.getRenderComputeType() +
                        "&blend_file=" + blenderProject.getBlendFilename() +
                        "&blender_version=" + blenderProject.getBlenderVersion() +
                        "&frame_filename=" + renderQueueItem.getBlenderFramePart().getFrameFileName() +
                        "&part_filename=" + renderQueueItem.getBlenderFramePart().getPartFilename() +
                        "&frame_number=" + renderQueueItem.getBlenderFramePart().getFrameNumber() +
                        "&part_number=" + renderQueueItem.getBlenderFramePart().getPartNumber() +
                        "&part_resolution_x=" + blenderProject.getResolutionX() +
                        "&part_resolution_y=" + blenderProject.getResolutionY() +
                        "&part_position_min_y=" + renderQueueItem.getBlenderFramePart().getPartPositionMinY() +
                        "&part_position_max_y=" + renderQueueItem.getBlenderFramePart().getPartPositionMaxY() +
                        "&part_res_percentage=" + blenderProject.getResPercentage() +
                        "&file_extension=" + renderQueueItem.getBlenderFramePart().getFileExtension();
                LOG.debug("Sending " + renderQueueItem + " to " + sethlansNode.getHostname());
                modifyingQueue = false;
                sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
            }
            modifyingQueue = false;
        }
    }

    private RenderQueueItem setQueueItemComputeType(SethlansNode sethlansNode, RenderQueueItem renderQueueItem) {
        // Before sending to a node the compute type must be either GPU or CPU,  CPU&GPU is only used for sorting at the server level.
        switch (sethlansNode.getComputeType()) {
            case CPU_GPU:
                if (sethlansNode.getCombinedGPURating() < sethlansNode.getCpuRating() && !sethlansNode.isGpuSlotInUse()) {
                    renderQueueItem.setRenderComputeType(ComputeType.GPU);
                    return renderQueueItem;
                } else if (sethlansNode.getCombinedGPURating() > sethlansNode.getCpuRating() && !sethlansNode.isCpuSlotInUse()) {
                    renderQueueItem.setRenderComputeType(ComputeType.CPU);
                    return renderQueueItem;
                } else if (sethlansNode.getCombinedGPURating() == sethlansNode.getCpuRating() && !sethlansNode.isCpuSlotInUse()) {
                    renderQueueItem.setRenderComputeType(ComputeType.CPU);
                    return renderQueueItem;

                } else if (sethlansNode.isCpuSlotInUse()) {
                    renderQueueItem.setRenderComputeType(ComputeType.GPU);
                    return renderQueueItem;

                } else if (sethlansNode.isGpuSlotInUse()) {
                    renderQueueItem.setRenderComputeType(ComputeType.CPU);
                    return renderQueueItem;
                }
                break;
            case GPU:
                renderQueueItem.setRenderComputeType(ComputeType.GPU);
                return renderQueueItem;
            case CPU:
                renderQueueItem.setRenderComputeType(ComputeType.CPU);
                return renderQueueItem;
        }
        return null;

    }

    private List<SethlansNode> getSortedNodeList(ComputeType computeType) {
        List<SethlansNode> sortedSethlansNodeList = new ArrayList<>();
        for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
            if (sethlansNode.getAvailableRenderingSlots() > 0 && sethlansNode.isBenchmarkComplete() && sethlansNode.isActive()) {
                SethlansUtils.listofNodes(computeType, sortedSethlansNodeList, sethlansNode);
            }
        }
        if (SethlansUtils.sortedNodeList(computeType, sortedSethlansNodeList)) {
            LOG.debug("Returned List:");
            for (SethlansNode sethlansNode : sortedSethlansNodeList) {
                LOG.debug(sethlansNode.getHostname() +
                        ": Available Slots(" + sethlansNode.getAvailableRenderingSlots() +
                        "). Compute Type(" + sethlansNode.getComputeType().getName() +
                        "). CPU in use(" + sethlansNode.isCpuSlotInUse() +
                        "). GPU in use (" + sethlansNode.isGpuSlotInUse() + ")");
            }
        }
        return sortedSethlansNodeList;
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


