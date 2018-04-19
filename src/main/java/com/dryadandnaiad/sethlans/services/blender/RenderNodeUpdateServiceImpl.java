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

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.node.NodeUpdate;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/16/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class RenderNodeUpdateServiceImpl implements RenderNodeUpdateService {
    private List<NodeUpdate> nodeQueue = new ArrayList<>();
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(RenderNodeUpdateServiceImpl.class);

    @Override
    public void addUpdateNodeItem(NodeUpdate nodeUpdate) {
        nodeQueue.add(nodeUpdate);
    }

    @Async
    @Override
    public void startRenderNodeUpdateQueue() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            LOG.debug("Stopping Render Node Update Queue Service");
        }
        while (true) {
            try {
                Thread.sleep(200);
                if (nodeQueue.size() > 0) {
                    LOG.debug("Running Node Update Queue");
                    if (nodeQueue.get(0).isInUse()) {
                        nodeUpdateTrue(nodeQueue.get(0));
                    } else {
                        nodeUpdateFalse(nodeQueue.get(0));
                    }
                    nodeQueue.remove(0);
                }
            } catch (InterruptedException e) {
                LOG.debug("Stopping Render Node Update Queue Service");
                break;
            }
        }
    }

    private void nodeUpdateFalse(NodeUpdate nodeUpdate) {
        SethlansNode sethlansNode = nodeUpdate.getSethlansNode();
        switch (nodeUpdate.getComputeType()) {
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
        sethlansNode.setVersion(sethlansNodeDatabaseService.getById(sethlansNode.getId()).getVersion());
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
    }

    private void nodeUpdateTrue(NodeUpdate nodeUpdate) {
        SethlansNode sethlansNode = nodeUpdate.getSethlansNode();
        switch (nodeUpdate.getComputeType()) {
            case CPU:
                sethlansNode.setCpuSlotInUse(true);
                sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
                if (sethlansNode.getAvailableRenderingSlots() < 0) {
                    sethlansNode.setAvailableRenderingSlots(0);
                }
                break;
            case GPU:
                sethlansNode.setGpuSlotInUse(true);
                sethlansNode.setAvailableRenderingSlots(sethlansNode.getAvailableRenderingSlots() - 1);
                if (sethlansNode.getAvailableRenderingSlots() < 0) {
                    sethlansNode.setAvailableRenderingSlots(0);
                }
                break;
            default:
                LOG.debug("Invalid compute type specified for rendering.");
        }
        LOG.debug(sethlansNode.getHostname() + " has " + sethlansNode.getAvailableRenderingSlots() + " available rendering slot(s)");
        sethlansNode.setVersion(sethlansNodeDatabaseService.getById(sethlansNode.getId()).getVersion());
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
