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
                            if (!blenderRenderQueueItem.isComplete() && !blenderRenderQueueItem.isRendering() && !blenderRenderQueueItem.isPaused()) {
                                if (count == 20) {
                                    LOG.debug(blenderRenderQueueItem.toString() + " is waiting to be rendered.");
                                }
                                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(blenderRenderQueueItem.getConnection_uuid());
                                if (sethlansNode.isActive() && !sethlansNode.isRendering()) {
                                    LOG.debug("Sending " + blenderRenderQueueItem + " to " + sethlansNode.getHostname());
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

    @Override
    public void pauseRenderQueueforProject(BlenderProject blenderProject) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.queueItemsByProjectUUID(blenderProject.getProject_uuid());
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.activeNodesRendering();
        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
            blenderRenderQueueItem.setPaused(true);
            for (SethlansNode sethlansNode : sethlansNodeList) {
                if (sethlansNode.getConnection_uuid().equals(blenderRenderQueueItem.getConnection_uuid())) {
                    sethlansNode.setRendering(false);
                    sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
                }
            }
            blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);

        }
    }

    @Override
    public void deleteRenderQueueforProject(BlenderProject blenderProject) {
        List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.queueItemsByProjectUUID(blenderProject.getProject_uuid());
        for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
            blenderRenderQueueDatabaseService.delete(blenderRenderQueueItem);
        }
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
            LOG.debug("Render Queue configured \n" + blenderRenderQueueDatabaseService.listPendingRender());
        } else {
            LOG.debug("No compatible rendering nodes found for this project.");
        }
    }


    private boolean nodesEquallyPowered(List<SethlansNode> sortedList) {
        LOG.debug("Comparing node strength");
        List<Integer> ratings = new ArrayList<>();
        Integer sum = 0;
        for (SethlansNode sethlansNode : sortedList) {
            ratings.add(sethlansNode.getCombinedCPUGPURating());
            sum += sethlansNode.getCombinedCPUGPURating();
        }
        Integer average = sum / sortedList.size();
        LOG.debug("Node Average: " + average);

        for (Integer rating : ratings) {
            if (Math.abs(average - rating) > 18000) {
                LOG.debug("Rating: " + rating);
                LOG.debug("Difference: " + Math.abs(average - rating));
                LOG.debug("Nodes are not equally powered, assigning a weight to each one in order of strength.");
                return false;
            }


        }
        LOG.debug("Nodes are equally powered, assigning the same weight to all");
        return true;

    }

    private RandomCollection<SethlansNode> getRandomWeightedNode(BlenderProject blenderProject) {
        RandomCollection<SethlansNode> nodeRandomCollection = new RandomCollection<>();
        List<SethlansNode> sortedList =
                SethlansUtils.getFastestNodes(sethlansNodeDatabaseService.listAll(), blenderProject.getRenderOn());
        if (sortedList != null) {
            LOG.debug("Sorted List " + sortedList.toString());
            for (int i = 0; i < sortedList.size(); i++) {
                LOG.debug("Current Node List " + sortedList);
                if (nodesEquallyPowered(sortedList)) {
                    LOG.debug("Adding " + sortedList.get(i).getHostname() + " to weighted list using equal values.");
                    nodeRandomCollection.add(sortedList.get(0).getCombinedCPUGPURating(), sortedList.get(i));
                } else {
                    LOG.debug("Adding " + sortedList.get(i).getHostname() + " to weighted list using it's original rating.");
                    nodeRandomCollection.add(sortedList.get(i).getCombinedCPUGPURating(), sortedList.get(i));
                    sortedList.remove(sortedList.get(i));
                    i--;
                }
            }

            LOG.debug("Sorted node list " + nodeRandomCollection.toString());

        } else {
            LOG.debug("Sorted List was empty.");
        }


        return nodeRandomCollection;
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
