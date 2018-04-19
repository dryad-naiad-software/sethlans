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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.node.NodeSlotUpdate;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 1/1/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderQueueServiceImpl implements BlenderQueueService {
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private NodeSlotUpdateService nodeSlotUpdateService;
    private static final Logger LOG = LoggerFactory.getLogger(BlenderQueueServiceImpl.class);
    private boolean populatingQueue;
    private boolean queueBeingPaused;

    @Override
    @Async
    public void startQueue() {


        int count = 0;
        int cycle = 20;
        //noinspection InfiniteLoopStatement
        while (true) {

            try {
                Thread.sleep(5000);
                if (!sethlansNodeDatabaseService.listAll().isEmpty() || !blenderRenderQueueDatabaseService.listAll().isEmpty()) {
                    if (!populatingQueue || !queueBeingPaused) {
                        LOG.debug("Processing Project Queue.");
                        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.listAll();
                        List<SethlansNode> listToSort = new ArrayList<>();
                        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                            if (!blenderRenderQueueItem.isComplete() && !blenderRenderQueueItem.isRendering() && !blenderRenderQueueItem.isPaused()) {
                                timedLog(count, cycle, blenderRenderQueueItem.toString() + " is waiting to be rendered.");
                                ComputeType computeType = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid()).getRenderOn();
                                listToSort = getSortedList(listToSort, computeType);
                                SethlansNode sethlansNode = listToSort.get(0);
                                LOG.debug("Assigned " + sethlansNode.getHostname() + " to queue item. Removing from sorted list.");
                                listToSort.remove(0);
                                if (sethlansNode != null && sethlansNode.isActive() && sethlansNode.getAvailableRenderingSlots() > 0 && !sethlansNode.isDisabled()) {
                                    blenderRenderQueueItem.setConnection_uuid(sethlansNode.getConnection_uuid());
                                    BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
                                    ComputeType projectComputeType = blenderProject.getRenderOn();
                                    sendQueueItemToNode(sethlansNode, projectComputeType, blenderProject, blenderRenderQueueItem);
                                } else {
                                    timedLog(count, cycle, "All nodes are busy. Will attempt in next loop. " + blenderRenderQueueItem.getBlenderFramePart());
                                    break;
                                }
                            }
                            if (!blenderRenderQueueItem.isComplete() && blenderRenderQueueItem.isRendering() && !blenderRenderQueueItem.isPaused()) {
                                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderRenderQueueItem.getConnection_uuid());
                                if (!sethlansNode.isActive()) {
                                    LOG.debug(sethlansNode.getHostname() + " is no longer active. Returning " + blenderRenderQueueItem + " to pending render state.");
                                    blenderRenderQueueItem.setRendering(false);
                                    blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                                }
                            }
                        }
                    }

                }
                Thread.sleep(1000);
                if (count == cycle) {
                    count = 0;
                } else {
                    count++;
                }

            } catch (InterruptedException e) {
                LOG.debug("Stopping Project Queue Service");
                break;
            } catch (NullPointerException e) {

            }
        }
    }

    private void sendQueueItemToNode(SethlansNode sethlansNode, ComputeType projectComputeType, BlenderProject blenderProject, BlenderRenderQueueItem blenderRenderQueueItem) throws InterruptedException {
        sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(sethlansNode.getConnection_uuid()); // Refresh node from DB first.
        // If both the project and the node is CPU and GPU, use the method with the lowest rating.
        if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU) && projectComputeType.equals(ComputeType.CPU_GPU)) {
            if (sethlansNode.getCombinedGPURating() < sethlansNode.getCpuRating() && !sethlansNode.isGpuSlotInUse()) {
                // Use GPU if it is free and is the faster benchmark
                projectComputeType = ComputeType.GPU;
            }
            if (sethlansNode.getCombinedGPURating() < sethlansNode.getCpuRating() && sethlansNode.isGpuSlotInUse()) {
                // Use CPU if the GPU is busy but the GPU is the faster benchmark
                projectComputeType = ComputeType.CPU;
            }

            if (sethlansNode.getCombinedGPURating() > sethlansNode.getCpuRating() && sethlansNode.isCpuSlotInUse()) {
                // Use the GPU if the CPU is the faster benchmark but is in use.
                projectComputeType = ComputeType.GPU;
            }

            if (sethlansNode.getCombinedGPURating() > sethlansNode.getCpuRating() && !sethlansNode.isCpuSlotInUse()) {
                // Use the CPU if it is free and is the faster benchmark.
                projectComputeType = ComputeType.CPU;
            }
        }

        if (projectComputeType.equals(ComputeType.CPU_GPU) && sethlansNode.getComputeType().equals(ComputeType.CPU)) {
            projectComputeType = ComputeType.CPU;
        }

        if (projectComputeType.equals(ComputeType.CPU_GPU) && sethlansNode.getComputeType().equals(ComputeType.GPU)) {
            projectComputeType = ComputeType.GPU;
        }

        String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" +
                sethlansNode.getNetworkPort() + "/api/render/request";
        String params = "project_name=" + blenderProject.getProjectName() +
                "&connection_uuid=" + sethlansNode.getConnection_uuid() +
                "&project_uuid=" + blenderProject.getProject_uuid() +
                "&render_output_format=" + blenderProject.getRenderOutputFormat() +
                "&samples=" + blenderProject.getSamples() +
                "&blender_engine=" + blenderProject.getBlenderEngine() +
                "&compute_type=" + projectComputeType +
                "&blend_file=" + blenderProject.getBlendFilename() +
                "&blender_version=" + blenderProject.getBlenderVersion() +
                "&frame_filename=" + blenderRenderQueueItem.getBlenderFramePart().getFrameFileName() +
                "&part_filename=" + blenderRenderQueueItem.getBlenderFramePart().getPartFilename() +
                "&frame_number=" + blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() +
                "&part_number=" + blenderRenderQueueItem.getBlenderFramePart().getPartNumber() +
                "&part_resolution_x=" + blenderProject.getResolutionX() +
                "&part_resolution_y=" + blenderProject.getResolutionY() +
                "&part_position_min_y=" + blenderRenderQueueItem.getBlenderFramePart().getPartPositionMinY() +
                "&part_position_max_y=" + blenderRenderQueueItem.getBlenderFramePart().getPartPositionMaxY() +
                "&part_res_percentage=" + blenderProject.getResPercentage() +
                "&file_extension=" + blenderRenderQueueItem.getBlenderFramePart().getFileExtension();


        if (!sethlansNode.isGpuSlotInUse() && projectComputeType == ComputeType.GPU) {

            sendToRemote(sethlansNode, projectComputeType, blenderRenderQueueItem, connectionURL, params);
        }

        if (!sethlansNode.isCpuSlotInUse() && projectComputeType == ComputeType.CPU) {

            sendToRemote(sethlansNode, projectComputeType, blenderRenderQueueItem, connectionURL, params);
        }

    }

    private void sendToRemote(SethlansNode sethlansNode, ComputeType projectComputeType, BlenderRenderQueueItem blenderRenderQueueItem, String connectionURL, String params) throws InterruptedException {

        sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(sethlansNode.getConnection_uuid()); // Refresh node from DB first.
        LOG.debug("Sending " + blenderRenderQueueItem + " to " + sethlansNode.getHostname());

        if (sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params)) {
            blenderRenderQueueItem.setRendering(true);
            blenderRenderQueueItem.setRenderComputeType(projectComputeType);

            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
            if (blenderProject.getProjectStatus() == ProjectStatus.Pending) {
                blenderProject.setProjectStatus(ProjectStatus.Started);
                long time = System.currentTimeMillis();
                blenderProject.setStartTime(time);
                blenderProject.setEndTime(time);
                blenderProject.setVersion(blenderProjectDatabaseService.getById(blenderProject.getId()).getVersion());
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            }
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);

            NodeSlotUpdate nodeSlotUpdate = new NodeSlotUpdate();
            nodeSlotUpdate.setComputeType(projectComputeType);
            nodeSlotUpdate.setInUse(true);
            nodeSlotUpdate.setOffline(false);
            nodeSlotUpdate.setViaQuery(false);
            nodeSlotUpdate.setSethlansNode(sethlansNode);
            nodeSlotUpdateService.addUpdateNodeItem(nodeSlotUpdate);
            Thread.sleep(1000);

        }
    }


    public boolean emptyRenderQueueforProject(BlenderProject blenderProject) {
        // Placeholder for stop/delete method.
        return false;
    }

    @Override
    public void pauseRenderQueueforProject(BlenderProject blenderProject) {
        queueBeingPaused = true;
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.queueItemsByProjectUUID(blenderProject.getProject_uuid());
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.activeNodesWithNoFreeSlots();
        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
            blenderRenderQueueItem.setPaused(true);
            for (SethlansNode sethlansNode : sethlansNodeList) {
                if (sethlansNode.getConnection_uuid().equals(blenderRenderQueueItem.getConnection_uuid())) {
                    sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() + 1);
                    if (sethlansNode.getAvailableRenderingSlots() > sethlansNode.getTotalRenderingSlots()) {
                        sethlansNode.setAvailableRenderingSlots(sethlansNode.getTotalRenderingSlots());
                    }
                    sethlansNode.setCpuSlotInUse(false);
                    sethlansNode.setGpuSlotInUse(false);
                    sethlansNode.setVersion(sethlansNodeDatabaseService.getByConnectionUUID(sethlansNode.getConnection_uuid()).getVersion());
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                }
            }
            blenderProject.setProjectStatus(ProjectStatus.Paused);
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);

        }
        queueBeingPaused = false;
    }

    @Override
    public void deleteRenderQueueforProject(BlenderProject blenderProject) {
        blenderRenderQueueDatabaseService.deleteAllByProject(blenderProject.getProject_uuid());
    }

    public boolean resumeRenderQueueforProject(BlenderProject blenderProject) {
        return false;
    }

    public boolean pauseQueueforAllProjects() {
        return false;
    }


    @Override
    public void populateProjectQueue(BlenderProject blenderProject) {
        populatingQueue = true;
        List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
        for (BlenderFramePart blenderFramePart : blenderFramePartList) {
            BlenderRenderQueueItem blenderRenderQueueItem = new BlenderRenderQueueItem();
            blenderRenderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
            blenderRenderQueueItem.setComplete(false);
            blenderRenderQueueItem.setPaused(false);
            blenderRenderQueueItem.setBlenderFramePart(blenderFramePart);
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
        }
        populatingQueue = false;
        LOG.debug("Render Queue configured, " + blenderRenderQueueDatabaseService.listPendingRender().size() + " items in queue");
    }

    private void timedLog(int count, int cycle, String message) {
        if (count == cycle) {
            LOG.debug(message);
        }
    }

    private List<SethlansNode> getSortedList(List<SethlansNode> listToSort, ComputeType computeType) {
        if (listToSort.size() == 0) {
            for (SethlansNode sethlansNode : sethlansNodeDatabaseService.listAll()) {
                if (sethlansNode.getAvailableRenderingSlots() > 0 && sethlansNode.isBenchmarkComplete() && sethlansNode.isActive()) {
                    SethlansUtils.listofNodes(computeType, listToSort, sethlansNode);
                }
            }
            if (SethlansUtils.sortedNodeList(computeType, listToSort)) {
                LOG.debug("Returned List:");
                for (SethlansNode sethlansNode : listToSort) {
                    LOG.debug(sethlansNode.getHostname() +
                            ": Available Slots(" + sethlansNode.getAvailableRenderingSlots() +
                            "). Compute Type(" + sethlansNode.getComputeType().getName() +
                            "). CPU in use(" + sethlansNode.isCpuSlotInUse() +
                            "). GPU in use (" + sethlansNode.isGpuSlotInUse() + ")");
                }
                return listToSort;
            }
        }

        return listToSort;
    }


    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setBlenderRenderQueueDatabaseService(BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService) {
        this.blenderRenderQueueDatabaseService = blenderRenderQueueDatabaseService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setNodeSlotUpdateService(NodeSlotUpdateService nodeSlotUpdateService) {
        this.nodeSlotUpdateService = nodeSlotUpdateService;
    }
}
