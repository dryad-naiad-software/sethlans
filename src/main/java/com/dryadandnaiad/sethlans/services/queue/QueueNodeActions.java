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
import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;
import com.dryadandnaiad.sethlans.domains.info.FreeDevice;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
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
                                        BlenderProjectDatabaseService blenderProjectDatabaseService,
                                        SethlansNodeDatabaseService sethlansNodeDatabaseService, List<ProcessNodeStatus> itemsProcessed, GetRawDataService getRawDataService
    ) {
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
        }
        itemsProcessed.add(processNodeStatus);
    }

    static void assignToNode(RenderQueueDatabaseService renderQueueDatabaseService, SethlansNodeDatabaseService sethlansNodeDatabaseService) {
//        List<RenderQueueItem> renderQueueItemList = renderQueueDatabaseService.listPendingRender();
//        int totalAvailableSlots = sethlansNodeDatabaseService.activeNodeswithFreeSlots().size();
//        if (renderQueueItemList.size() > 0 && totalAvailableSlots > 0) {
//            int count;
//            if (totalAvailableSlots > renderQueueItemList.size()) {
//                count = renderQueueItemList.size();
//            } else {
//                count = totalAvailableSlots;
//            }
//            List<SethlansNode> nodesToUpdate = new ArrayList<>();
//            for (int i = 0; i < count; i++) {
//                RenderQueueItem renderQueueItem = renderQueueItemList.get(i);
//                if (!renderQueueItem.isRendering()) {
//                    LOG.debug(renderQueueItem.getProjectName() + " uuid: " +
//                            renderQueueItem.getProjectUUID() + " Frame: "
//                            + renderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
//                            + renderQueueItem.getBlenderFramePart().getPartNumber() + " is waiting to be rendered.");
//                    renderQueueItem = setQueueItemToNode(sethlansNodeDatabaseService, i, nodesToUpdate, renderQueueItem);
//                    switch (renderQueueItem.getRenderComputeType()) {
//                        case GPU:
//                            if (renderQueueItem.getGpuDeviceId() != null) {
//                                updateRenderQueueItem(renderQueueItem, renderQueueDatabaseService);
//                            } else {
//                                LOG.debug("No free GPU available for rendering.");
//                            }
//                            break;
//                        case CPU:
//                            updateRenderQueueItem(renderQueueItem, renderQueueDatabaseService);
//                            break;
//                        case CPU_GPU:
//                            updateRenderQueueItem(renderQueueItem, renderQueueDatabaseService);
//                            break;
//                    }
//
//                }
//            }
//            for (SethlansNode sethlansNode : nodesToUpdate) {
//                sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
//            }
//        }
    }

    private static void updateRenderQueueItem(RenderQueueItem renderQueueItem, RenderQueueDatabaseService renderQueueDatabaseService) {
        LOG.debug(renderQueueItem.getProjectName() + " uuid: " +
                renderQueueItem.getProjectUUID() + " Frame: "
                + renderQueueItem.getBlenderFramePart().getFrameNumber() + " Part: "
                + renderQueueItem.getBlenderFramePart().getPartNumber() + " is updated.");
        renderQueueDatabaseService.saveOrUpdate(renderQueueItem);
    }

    private static RenderQueueItem setQueueItemToNode(SethlansNodeDatabaseService sethlansNodeDatabaseService, int i, List<SethlansNode> nodesToUpdate, RenderQueueItem renderQueueItem, GetRawDataService getRawDataService) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.activeNodeList();
        Gson gson = new Gson();
        List<FreeDevice> freeDeviceList = new ArrayList<>();
        for (SethlansNode sethlansNode : sethlansNodeList) {
            List<String> deviceIdsInUse =
                    gson.fromJson(getRawDataService.getNodeResult("https://" + sethlansNode.getIpAddress() + ":" + sethlansNode.getNetworkPort() +
                            "/api/info/used_device_ids"), new TypeToken<List<String>>() {
                    }.getType());
            List<String> availableDeviceIds = SethlansNodeUtils.getAvailableDeviceIds(sethlansNode, deviceIdsInUse);
            for (String availableDeviceId : availableDeviceIds) {
                freeDeviceList.add(new FreeDevice(sethlansNode.getId(), availableDeviceId, SethlansNodeUtils.getDeviceIdBenchmark(sethlansNode, availableDeviceId)));
            }
        }

        // TODO poll nodes in order to get available slots and which device id's are available, a class or a map to hold the following information;
        //  node_id, available deviceid, benchmark rating for selected deviceid
//        switch (renderQueueItem.getRenderComputeType()) {
//            case CPU_GPU:
//                sortedSethlansNodeList = getSortedNodeList(ComputeType.CPU_GPU, sethlansNodeDatabaseService);
//                nodeStatusLog(i, sortedSethlansNodeList);
//                if (sortedSethlansNodeList != null) {
//                    sethlansNode = sortedSethlansNodeList.get(i);
//                    sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
//                    renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
//                    renderQueueItem = setQueueItemComputeType(sethlansNode, renderQueueItem);
//                    LOG.debug(renderQueueItem.toString());
//                    switch (renderQueueItem.getRenderComputeType()) {
//                        case CPU:
//                            sethlansNode.setCpuSlotInUse(true);
//                            break;
//                        case GPU:
//                            setAllGPUSlotState(renderQueueItem, sethlansNode);
//                            break;
//                        case CPU_GPU:
//                            LOG.error("Failure in logic this message should not be displayed.");
//                            break;
//                    }
//                    nodesToUpdate.add(sethlansNode);
//                }
//                break;
//            case CPU:
//                sortedSethlansNodeList = getSortedNodeList(ComputeType.CPU, sethlansNodeDatabaseService);
//                nodeStatusLog(i, sortedSethlansNodeList);
//                if (sortedSethlansNodeList != null) {
//                    sethlansNode = sortedSethlansNodeList.get(i);
//                    renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
//                    sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
//                    sethlansNode.setCpuSlotInUse(true);
//                    nodesToUpdate.add(sethlansNode);
//                }
//                break;
//            case GPU:
//                sortedSethlansNodeList = getSortedNodeList(ComputeType.GPU, sethlansNodeDatabaseService);
//                nodeStatusLog(i, sortedSethlansNodeList);
//                if (sortedSethlansNodeList != null) {
//                    sethlansNode = sortedSethlansNodeList.get(i);
//                    renderQueueItem.setConnectionUUID(sethlansNode.getConnectionUUID());
//                    sethlansNode.setAvailableRenderingSlots(Math.max(0, sethlansNode.getAvailableRenderingSlots() - 1));
//                    setAllGPUSlotState(renderQueueItem, sethlansNode);
//                    nodesToUpdate.add(sethlansNode);
//                }
//                break;
//        }
        return renderQueueItem;
    }


    static void sendQueueItemsToNodes(RenderQueueDatabaseService renderQueueDatabaseService,
                                      BlenderProjectDatabaseService blenderProjectDatabaseService,
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
                    "&gpu_device_id=" + renderQueueItem.getGpuDeviceId() +
                    "&queue_item_uuid=" + renderQueueItem.getQueueItemUUID() +
                    "&render_output_format=" + blenderProject.getRenderOutputFormat() +
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
            LOG.debug("Sending " + renderQueueItem + " to " + sethlansNode.getHostname());
            sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
        }
    }


}
