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
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.RandomCollection;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    private static final Logger LOG = LoggerFactory.getLogger(BlenderQueueServiceImpl.class);

    @Override
    @Async
    public void startQueue() {
        try {
            Thread.sleep(12000);

            int count = 0;
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    if (!sethlansNodeDatabaseService.listAll().isEmpty() || !blenderRenderQueueDatabaseService.listAll().isEmpty()) {
                        LOG.debug("Processing Render Queue. Verbose status set for every 5 minutes.");
                        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.listAll();
                        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                            if (!blenderRenderQueueItem.isRendering() || !blenderRenderQueueItem.isPaused() || !blenderRenderQueueItem.isComplete()) {
                                if (count == 20) {
                                    LOG.debug(blenderRenderQueueItem.getBlenderFramePart() + " is waiting to be rendered.");
                                }
                                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderRenderQueueItem.getConnection_uuid());
                                if (sethlansNode.isActive() && !sethlansNode.isRendering()) {
                                    LOG.debug("Sending " + blenderRenderQueueItem.getBlenderFramePart() + " to " + sethlansNode.getHostname());
                                    BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid());
                                    ComputeType projectComputeType = blenderProject.getRenderOn();

                                    // If both the project and the node is CPU and GPU, use the method with the lowest rating.
                                    if (sethlansNode.getComputeType().equals(ComputeType.CPU_GPU) && projectComputeType.equals(ComputeType.CPU_GPU)) {
                                        if (sethlansNode.getCombinedGPURating() < sethlansNode.getCpuRating()) {
                                            projectComputeType = ComputeType.GPU;
                                        } else {
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


                                    if (sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params)) {
                                        blenderRenderQueueItem.setRendering(true);
                                        sethlansNode.setRendering(true);
                                        blenderProject.setStarted(true);
                                        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                                    }
                                } else if (sethlansNode.isActive() && sethlansNode.isRendering()) {

                                    if (count == 20) {
                                        LOG.debug(sethlansNode.getHostname() + " is busy. Will attempt in next loop. " + blenderRenderQueueItem.getBlenderFramePart());
                                    }
                                } else if (!sethlansNode.isActive()) {
                                    LOG.debug(sethlansNode.getHostname() + " no longer active, reassigning " + blenderRenderQueueItem.getBlenderFramePart() +
                                            " to another node ");
                                    RandomCollection<SethlansNode> randomWeightedNode =
                                            getRandomWeightedNode(blenderProjectDatabaseService.getByProjectUUID(blenderRenderQueueItem.getProject_uuid()));
                                    blenderRenderQueueItem.setConnection_uuid(randomWeightedNode.next().getConnection_uuid());
                                }
                                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
                                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                            }

                        }
                        if (count == 20) {
                            count = 0;
                        }
                        count++;
                    }
                    Thread.sleep(15000);

                } catch (InterruptedException e) {
                    LOG.debug("Stopping Blender Queue Service");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean emptyRenderQueueforProject(BlenderProject blenderProject) {
        // Placeholder for stop/delete method.
        return false;
    }

    public boolean pauseRenderQueueforProject(BlenderProject blenderProject) {
        return false;
    }

    public boolean resumeRenderQueueforProject(BlenderProject blenderProject) {
        return false;
    }

    public boolean pauseQueueforAllProjects() {
        return false;
    }


    @Override
    public void populateRenderQueue(BlenderProject blenderProject) {
        RandomCollection<SethlansNode> randomWeightedNode = getRandomWeightedNode(blenderProject);
        if (randomWeightedNode.next() != null) {
            List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
            for (BlenderFramePart blenderFramePart : blenderFramePartList) {
                BlenderRenderQueueItem blenderRenderQueueItem = new BlenderRenderQueueItem();
                blenderRenderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
                blenderRenderQueueItem.setConnection_uuid(randomWeightedNode.next().getConnection_uuid());
                blenderRenderQueueItem.setComplete(false);
                blenderRenderQueueItem.setPaused(false);
                blenderRenderQueueItem.setBlenderFramePart(blenderFramePart);
                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            }
            LOG.debug("Render Queue configured \n" + blenderRenderQueueDatabaseService.listAll());
        } else {
            LOG.debug("No compatible rendering nodes found for this project.");
        }
    }

    private RandomCollection<SethlansNode> getRandomWeightedNode(BlenderProject blenderProject) {
        RandomCollection<SethlansNode> nodeRandomCollection = new RandomCollection<>();
        List<SethlansNode> sortedList =
                SethlansUtils.getFastestNodes(sethlansNodeDatabaseService.listAll(), blenderProject.getRenderOn());
        if (sortedList != null) {
            double weight = sortedList.size();
            LOG.debug("Sorted List " + sortedList.toString());
            for (int i = 0; i < sortedList.size(); i++) {
                if (i == 0) {
                    nodeRandomCollection.add(weight, sortedList.get(i));
                } else {
                    weight = weight * 0.75;
                    nodeRandomCollection.add(weight, sortedList.get(i));
                }
            }
        }
        LOG.error("Sorted List was empty.");

        return nodeRandomCollection;
    }

    private SethlansNode selectNodeToRenderWith(ComputeType computeType) {
        List<SethlansNode> sethlansNodes = sethlansNodeDatabaseService.listAll();
        SethlansNode selectedRenderNode;
        if (!computeType.equals(ComputeType.CPU_GPU)) {
            selectedRenderNode = SethlansUtils.getFastestFreeNode(sethlansNodes, computeType);
        } else {
            SethlansNode cpuNode = SethlansUtils.getFastestFreeNode(sethlansNodes, ComputeType.CPU);
            SethlansNode gpuNode = SethlansUtils.getFastestFreeNode(sethlansNodes, ComputeType.GPU);
            if (gpuNode == null) {
                return selectedRenderNode = cpuNode;
            }
            if (cpuNode == null) {
                return selectedRenderNode = gpuNode;
            }
            int gpuValue = gpuNode.getCombinedGPURating();
            int cpuValue = cpuNode.getCpuRating();
            if (cpuValue > gpuValue) {
                selectedRenderNode = gpuNode;
            } else if (cpuValue < gpuValue) {
                selectedRenderNode = cpuNode;

            } else {
                // If both CPU and GPU are equal, default to CPU node(generally has more memory able to handle bigger renders.)
                selectedRenderNode = cpuNode;
            }

        }
        if (selectedRenderNode != null) {
            return selectedRenderNode;
        } else {
            return null;
        }


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
}
