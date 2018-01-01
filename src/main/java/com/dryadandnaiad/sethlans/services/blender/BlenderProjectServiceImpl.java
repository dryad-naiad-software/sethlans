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
import com.dryadandnaiad.sethlans.utils.RandomCollection;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderProjectServiceImpl implements BlenderProjectService {
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectServiceImpl.class);

    @Override
    public void startProject(BlenderProject blenderProject) {
        configureFrameList(blenderProject);
        if (populateRenderQueue(blenderProject)) {
            RandomCollection<SethlansNode> nodeRandomCollection = new RandomCollection<>();
            List<SethlansNode> sortedList =
                    SethlansUtils.getFastestNodes(sethlansNodeDatabaseService.listAll(), blenderProject.getRenderOn());
            double weight = 50.0;
            for (int i = 0; i < sortedList.size(); i++) {
                nodeRandomCollection.add(weight - i, sortedList.get(i));
            }
            LOG.debug(nodeRandomCollection.toString());

        }

        // TODO logic to handle the queue depending on the number of ndoes available.
        // TODO weighted distribution of queue depending on speed of nodes.
    }

    @Override
    public void restartProject(BlenderProject blenderProject) {

    }

    private boolean populateRenderQueue(BlenderProject blenderProject) {
        if (selectNodeToRenderWith(blenderProject.getRenderOn()) != null) {
            List<BlenderFramePart> blenderFramePartList = blenderProject.getFramePartList();
            for (BlenderFramePart blenderFramePart : blenderFramePartList) {
                BlenderRenderQueueItem blenderRenderQueueItem = new BlenderRenderQueueItem();
                blenderRenderQueueItem.setProject_uuid(blenderProject.getProject_uuid());
                blenderRenderQueueItem.setComplete(false);
                blenderRenderQueueItem.setBlenderFramePart(blenderFramePart);
                blenderRenderQueueDatabaseService.saveOrUpdate(blenderRenderQueueItem);
            }
            LOG.debug("Render Queue configured \n" + blenderRenderQueueDatabaseService.listAll());
            return true;
        } else {
            LOG.debug("No compatible rendering nodes found for this project.");
            return false;
        }
    }

    private void configureFrameList(BlenderProject blenderProject) {
        List<BlenderFramePart> blenderFramePartList = new ArrayList<>();
        List<String> frameFileNames = new ArrayList<>();
        for (int i = 0; i < blenderProject.getTotalNumOfFrames(); i++) {
            frameFileNames.add(blenderProject.getProject_uuid() + "-" + (i + 1));
            for (int j = 0; j < blenderProject.getPartsPerFrame(); j++) {
                BlenderFramePart blenderFramePart = new BlenderFramePart();
                blenderFramePart.setFrameFileName(frameFileNames.get(i));
                blenderFramePart.setFrameNumber(i + 1);
                blenderFramePart.setPartNumber(j + 1);
                blenderFramePart.setPartFilename(blenderFramePart.getFrameFileName() + "-part" + (j + 1));
                blenderFramePart.setFileExtension(blenderProject.getRenderOutputFormat().name().toLowerCase());
                blenderFramePartList.add(blenderFramePart);

            }
        }
        blenderProject.setFrameFileNames(frameFileNames);
        blenderProject.setFramePartList(blenderFramePartList);
        LOG.debug("Project Frames configured \n" + blenderFramePartList);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);

    }

    @Override
    public void pauseProject(BlenderProject blenderProject) {
    }

    @Override
    public void stopProject(BlenderProject blenderProject) {
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
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderRenderQueueDatabaseService(BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService) {
        this.blenderRenderQueueDatabaseService = blenderRenderQueueDatabaseService;
    }
}
