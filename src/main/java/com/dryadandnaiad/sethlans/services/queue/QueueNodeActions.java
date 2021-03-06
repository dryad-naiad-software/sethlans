/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessNodeStatus;
import com.dryadandnaiad.sethlans.domains.database.queue.QueueHistoryItem;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.domains.info.AvailableDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.QueueHistoryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.AvailableDeviceComparator;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
class QueueNodeActions {

    private static final Logger LOG = LoggerFactory.getLogger(QueueNodeActions.class);

    static void processAcknowledgements(ProcessNodeStatus processNodeStatus,
                                        RenderQueueDatabaseService renderQueueDatabaseService,
                                        BlenderProjectDatabaseService blenderProjectDatabaseService, QueueHistoryDatabaseService queueHistoryDatabaseService,
                                        SethlansNodeDatabaseService sethlansNodeDatabaseService, List<ProcessNodeStatus> itemsProcessed) {
        if (processNodeStatus.isAccepted()) {
            RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processNodeStatus.getQueueUUID());
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProjectUUID());
            LOG.debug("Compute Type " + renderQueueItem.getRenderComputeType());
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(renderQueueItem.getConnectionUUID());
            LOG.debug("Received node acknowledgement from " + sethlansNode.getHostname() + " for queue item " + processNodeStatus.getQueueUUID());
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Pending)) {
                blenderProject.setProjectStatus(ProjectStatus.Started);
                blenderProject.setTimerStart(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
                blenderProject.setVersion(blenderProjectDatabaseService.getByIdWithoutFrameParts(blenderProject.getId()).getVersion());
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            }
            renderQueueItem.setRendering(true);
            renderQueueItem.setVersion(renderQueueDatabaseService.getByQueueUUID(processNodeStatus.getQueueUUID()).getVersion());
            renderQueueDatabaseService.saveOrUpdate(renderQueueItem);

            QueueHistoryItem queueHistoryItem = queueHistoryDatabaseService.getQueueHistoryItemToUpdate(renderQueueItem.getQueueItemUUID(), sethlansNode.getHostname(), renderQueueItem.getDeviceId());
            queueHistoryItem.setRendering(true);
            queueHistoryDatabaseService.saveOrUpdate(queueHistoryItem);
        }
        if (!processNodeStatus.isAccepted()) {
            RenderQueueItem renderQueueItem = renderQueueDatabaseService.getByQueueUUID(processNodeStatus.getQueueUUID());
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUIDWithoutFrameParts(renderQueueItem.getProjectUUID());
            LOG.debug("Received rejection for queue item " + processNodeStatus.getQueueUUID() + " adding back to pending queue.");

            renderQueueItem.setRenderComputeType(blenderProject.getRenderOn());
            renderQueueItem.setConnectionUUID(null);
            renderQueueItem.setRendering(false);
            renderQueueItem.setVersion(renderQueueDatabaseService.getByQueueUUID(processNodeStatus.getQueueUUID()).getVersion());
            renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
            QueueHistoryItem queueHistoryItem = queueHistoryDatabaseService.getQueueHistoryItemToUpdate(renderQueueItem.getQueueItemUUID(), sethlansNodeDatabaseService.getByConnectionUUID(renderQueueItem.getConnectionUUID()).getHostname(), renderQueueItem.getDeviceId());
            queueHistoryItem.setRendering(false);
            queueHistoryItem.setFailed(true);
            queueHistoryDatabaseService.saveOrUpdate(queueHistoryItem);
        }
        itemsProcessed.add(processNodeStatus);
    }

    static void queueItemToNode(RenderQueueDatabaseService renderQueueDatabaseService, SethlansNodeDatabaseService sethlansNodeDatabaseService, GetRawDataService getRawDataService) {
        if (renderQueueDatabaseService.listPendingRender().size() > 0) {
            List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.activeNodeList();
            Gson gson = new Gson();
            List<AvailableDevice> availableDeviceList = new ArrayList<>();
            for (SethlansNode sethlansNode : sethlansNodeList) {
                List<String> deviceIdsInUse =
                        gson.fromJson(getRawDataService.getNodeResult("https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() +
                                "/api/info/used_device_ids"), new TypeToken<List<String>>() {
                        }.getType());
                List<String> availableDeviceIds = SethlansNodeUtils.getAvailableDeviceIds(sethlansNode, deviceIdsInUse);
                for (String availableDeviceId : availableDeviceIds) {
                    availableDeviceList.add(new AvailableDevice(sethlansNode.getId(), availableDeviceId, SethlansNodeUtils.getDeviceIdBenchmark(sethlansNode, availableDeviceId), false));
                }
            }
            availableDeviceList.sort(new AvailableDeviceComparator());
            for (AvailableDevice availableDevice : availableDeviceList) {
                if (!availableDevice.isAssigned()) {
                    LOG.debug(availableDevice.toString());
                    assignItemToNode(renderQueueDatabaseService, sethlansNodeDatabaseService, availableDevice);
                }
            }
        }

    }

    private static void assignItemToNode(RenderQueueDatabaseService renderQueueDatabaseService, SethlansNodeDatabaseService sethlansNodeDatabaseService, AvailableDevice availableDevice) {
        List<RenderQueueItem> renderQueueItemList = renderQueueDatabaseService.listPendingRender();
        for (RenderQueueItem renderQueueItem : renderQueueItemList) {
            if (!availableDevice.isAssigned()) {
                if (!renderQueueItem.isRendering() && renderQueueItem.getConnectionUUID() == null) {
                    LOG.debug(renderQueueItem.getProjectName() + " uuid: " +
                            renderQueueItem.getProjectUUID() + " Frame: "
                            + renderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
                            + renderQueueItem.getBlenderFramePart().getPartNumber() + " is waiting to be rendered.");
                    SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(availableDevice.getId());
                    switch (renderQueueItem.getRenderComputeType()) {
                        case CPU_GPU:
                            renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
                            renderQueueItem.setDeviceId(availableDevice.getDeviceId());
                            availableDevice.setAssigned(true);
                            if (availableDevice.getDeviceId().equals("CPU")) {
                                renderQueueItem.setRenderComputeType(ComputeType.CPU);
                            } else {
                                renderQueueItem.setRenderComputeType(ComputeType.GPU);
                            }
                            break;
                        case CPU:
                            if (availableDevice.getDeviceId().equals("CPU")) {
                                renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
                                renderQueueItem.setDeviceId(availableDevice.getDeviceId());
                                availableDevice.setAssigned(true);
                            }
                            break;
                        case GPU:
                            if (!availableDevice.getDeviceId().equals("CPU")) {
                                renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
                                renderQueueItem.setDeviceId(availableDevice.getDeviceId());
                                availableDevice.setAssigned(true);
                            }
                            break;
                    }
                    LOG.debug(availableDevice.toString());
                    LOG.debug(renderQueueItem.toString());
                    updateRenderQueueItem(renderQueueItem, renderQueueDatabaseService);
                }
            }

        }
    }


    private static void updateRenderQueueItem(RenderQueueItem renderQueueItem, RenderQueueDatabaseService renderQueueDatabaseService) {
        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
    }

    static void sendQueueItemsToNodes(RenderQueueDatabaseService renderQueueDatabaseService,
                                      BlenderProjectDatabaseService blenderProjectDatabaseService, QueueHistoryDatabaseService queueHistoryDatabaseService,
                                      SethlansNodeDatabaseService sethlansNodeDatabaseService, SethlansAPIConnectionService sethlansAPIConnectionService) {
        List<RenderQueueItem> renderQueueItemList = renderQueueDatabaseService.listPendingRenderWithNodeAssigned();
        for (RenderQueueItem renderQueueItem : renderQueueItemList) {
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(renderQueueItem.getProjectUUID());
            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(renderQueueItem.getConnectionUUID());
            String connectionURL = "https://" + sethlansNode.getIpAddress() + ":" +
                    sethlansNode.getNetworkPort() + "/api/render/request";
            String params = "project_name=" + blenderProject.getProjectName() +
                    "&connection_uuid=" + sethlansNode.getConnectionUUID() +
                    "&project_uuid=" + blenderProject.getProjectUUID() +
                    "&device_id=" + renderQueueItem.getDeviceId() +
                    "&queue_item_uuid=" + renderQueueItem.getQueueItemUUID() +
                    "&render_output_format=" + blenderProject.getImageOutputFormat() +
                    "&samples=" + blenderProject.getSamples() +
                    "&blender_engine=" + blenderProject.getBlenderEngine() +
                    "&compute_type=" + renderQueueItem.getRenderComputeType() +
                    "&blend_file=" + blenderProject.getBlendFilename() +
                    "&blend_file_md5=" + blenderProject.getBlendFilenameMD5Sum() +
                    "&blender_version=" + blenderProject.getBlenderVersion() +
                    "&frame_filename=" + renderQueueItem.getBlenderFramePart().getFrameFileName() +
                    "&part_filename=" + renderQueueItem.getBlenderFramePart().getPartFilename() +
                    "&frame_number=" + renderQueueItem.getBlenderFramePart().getFrameNumber() +
                    "&part_number=" + renderQueueItem.getBlenderFramePart().getPartNumber() +
                    "&part_resolution_x=" + blenderProject.getResolutionX() +
                    "&part_resolution_y=" + blenderProject.getResolutionY() +
                    "&part_position_min_x=" + renderQueueItem.getBlenderFramePart().getPartPositionMinX() +
                    "&part_position_max_x=" + renderQueueItem.getBlenderFramePart().getPartPositionMaxX() +
                    "&part_position_min_y=" + renderQueueItem.getBlenderFramePart().getPartPositionMinY() +
                    "&part_position_max_y=" + renderQueueItem.getBlenderFramePart().getPartPositionMaxY() +
                    "&part_res_percentage=" + blenderProject.getResPercentage() +
                    "&file_extension=" + renderQueueItem.getBlenderFramePart().getFileExtension();
            LOG.debug(renderQueueItem.getProjectName() + " uuid: " +
                    renderQueueItem.getProjectUUID() + " Frame: "
                    + renderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
                    + renderQueueItem.getBlenderFramePart().getPartNumber() + " has been assigned to " + renderQueueItem.getDeviceId() + " on " + sethlansNode.getHostname());
            LOG.debug("Sending " + renderQueueItem + " to " + sethlansNode.getHostname());
            sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
            createQueueHistoryItem(renderQueueItem, queueHistoryDatabaseService, sethlansNode, blenderProject);
        }
    }

    private static void createQueueHistoryItem(RenderQueueItem renderQueueItem, QueueHistoryDatabaseService queueHistoryDatabaseService, SethlansNode node, BlenderProject blenderProject) {
        QueueHistoryItem queueHistoryItem = new QueueHistoryItem();
        queueHistoryItem.setComputeType(renderQueueItem.getRenderComputeType());
        queueHistoryItem.setComplete(false);
        queueHistoryItem.setDeviceId(renderQueueItem.getDeviceId());
        queueHistoryItem.setEngine(blenderProject.getBlenderEngine());
        queueHistoryItem.setProjectName(blenderProject.getProjectName());
        queueHistoryItem.setProjectLocation(blenderProject.getProjectRootDir());
        queueHistoryItem.setNodeName(node.getHostname());
        queueHistoryItem.setFailed(false);
        queueHistoryItem.setPaused(false);
        queueHistoryItem.setProjectUUID(blenderProject.getProjectUUID());
        queueHistoryItem.setQueueItemUUID(renderQueueItem.getQueueItemUUID());
        queueHistoryItem.setRendering(false);
        queueHistoryItem.setFrameAndPartNumbers(renderQueueItem.getBlenderFramePart().getFrameNumber() + ":" + renderQueueItem.getBlenderFramePart().getPartNumber());
        queueHistoryItem.setTaskDate(System.currentTimeMillis());
        queueHistoryDatabaseService.saveOrUpdate(queueHistoryItem);
    }


}
