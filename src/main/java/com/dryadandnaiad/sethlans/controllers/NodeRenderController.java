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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.render.RenderTaskHistory;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.blender.benchmark.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.blender.render.BlenderRenderService;
import com.dryadandnaiad.sethlans.services.database.BenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskHistoryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 12/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
public class NodeRenderController {
    @Value("${sethlans.gpu_id}")
    private String cuda;

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private RenderTaskDatabaseService renderTaskDatabaseService;
    private BenchmarkTaskDatabaseService benchmarkTaskDatabaseService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private BlenderRenderService blenderRenderService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private RenderTaskHistoryDatabaseService renderTaskHistoryDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeRenderController.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @PostMapping(value = "/api/render/cancel")
    public void cancelRender(@RequestParam String queue_item_uuid, @RequestParam String connection_uuid) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.error("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            RenderTask tasktoCancel = renderTaskDatabaseService.findByQueueUUID(queue_item_uuid);
            if (tasktoCancel != null) {
                LOG.info("Attempting to cancel task with uuid " + queue_item_uuid);
                tasktoCancel.setCancelRequestReceived(true);
                renderTaskDatabaseService.saveOrUpdate(tasktoCancel);
                int slotsInUse = (int) renderTaskDatabaseService.tableSize();
                for (RenderTask renderTask : renderTaskDatabaseService.listAll()) {
                    if (renderTask.isCancelRequestReceived()) {
                        slotsInUse--;
                    }
                }
                SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
                String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_slot_update/";
                String params = "connection_uuid=" + connection_uuid + "&device_id=" + tasktoCancel.getDeviceID() + "&available_slots=" + SethlansNodeUtils.getAvailableSlots(slotsInUse);
                sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);

            } else {
                LOG.info("Unable to cancel task with uuid " + queue_item_uuid + ", task not found.");
            }

        }

    }

    @PostMapping(value = "/api/render/request")
    public void renderRequest(@RequestParam String project_name, @RequestParam String connection_uuid,
                              @RequestParam String project_uuid,
                              @RequestParam String queue_item_uuid, @RequestParam String device_id,
                              @RequestParam ImageOutputFormat render_output_format,
                              @RequestParam int samples, @RequestParam BlenderEngine blender_engine,
                              @RequestParam ComputeType compute_type,
                              @RequestParam String blend_file, @RequestParam String blend_file_md5,
                              @RequestParam String blender_version,
                              @RequestParam String frame_filename, @RequestParam int frame_number, @RequestParam int part_number,
                              @RequestParam int part_resolution_x, @RequestParam int part_resolution_y,
                              @RequestParam double part_position_min_x, @RequestParam double part_position_max_x,
                              @RequestParam double part_position_min_y, @RequestParam double part_position_max_y,
                              @RequestParam int part_res_percentage, @RequestParam String part_filename, @RequestParam String file_extension) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.error("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            ComputeType computeType = ComputeType.valueOf(getProperty(SethlansConfigKeys.COMPUTE_METHOD.toString()));
            NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
            LOG.info("Render Request Received, preparing render task.");
            List<RenderTask> renderTaskList = renderTaskDatabaseService.listAll();
            boolean rejected = false;

            switch (computeType) {
                case CPU_GPU:
                    if (compute_type.equals(ComputeType.GPU)) {
                        if (device_id == null || device_id.equals("null") || device_id.equals("")) {
                            rejectRequest(connection_uuid, queue_item_uuid);
                            rejected = true;
                        }
                    }
                    if (nodeInfo.isCombined()) {
                        if (renderTaskList.size() == 2) {
                            rejectRequest(connection_uuid, queue_item_uuid);
                            rejected = true;
                        }
                    } else {
                        if (renderTaskList.size() == (nodeInfo.getSelectedGPUs().size() + 1)) {
                            rejectRequest(connection_uuid, queue_item_uuid);
                            rejected = true;
                        }
                    }
                    break;
                case GPU:
                    if (device_id == null || device_id.equals("null")) {
                        rejectRequest(connection_uuid, queue_item_uuid);
                        rejected = true;
                    }
                    if (nodeInfo.isCombined()) {
                        if (renderTaskList.size() == 1) {
                            rejectRequest(connection_uuid, queue_item_uuid);
                            rejected = true;
                        }
                    } else {
                        if (renderTaskList.size() == nodeInfo.getSelectedGPUs().size()) {
                            rejectRequest(connection_uuid, queue_item_uuid);
                            rejected = true;
                        }
                    }
                    break;
                case CPU:
                    if (renderTaskList.size() == 1) {
                        rejectRequest(connection_uuid, queue_item_uuid);
                        rejected = true;
                    }
                    break;
            }
            if (!rejected) {
                LOG.debug("Adding task to render queue.");
                SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
                String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_accept_item/";
                String params = "queue_item_uuid=" + queue_item_uuid;
                sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
                RenderTask renderTask;
                BlenderFramePart framePart = new BlenderFramePart();
                framePart.setFrameNumber(frame_number);
                framePart.setFileExtension(file_extension);
                framePart.setFrameFileName(frame_filename);
                framePart.setPartFilename(part_filename);
                framePart.setPartNumber(part_number);
                framePart.setPartPositionMaxX(part_position_max_x);
                framePart.setPartPositionMinX(part_position_min_x);
                framePart.setPartPositionMinY(part_position_min_y);
                framePart.setPartPositionMaxY(part_position_max_y);

                // Create a new task
                renderTask = new RenderTask();
                renderTask.setProjectUUID(project_uuid);
                renderTask.setProjectName(project_name);
                renderTask.setRenderTaskUUID(UUID.randomUUID().toString());
                renderTask.setServerQueueUUID(queue_item_uuid);
                renderTask.setConnectionUUID(connection_uuid);
                renderTask.setImageOutputFormat(render_output_format);
                renderTask.setSamples(samples);
                renderTask.setBlenderEngine(blender_engine);
                renderTask.setComputeType(compute_type);
                renderTask.setBlendFilename(blend_file);
                renderTask.setBlendFileMD5Sum(blend_file_md5);
                renderTask.setBlenderVersion(blender_version);
                renderTask.setBlenderFramePart(framePart);
                renderTask.setTaskResolutionX(part_resolution_x);
                renderTask.setTaskResolutionY(part_resolution_y);
                renderTask.setPartResPercentage(part_res_percentage);
                renderTask.setDeviceID(device_id);
                renderTask.setComplete(false);
                renderTask.setCancelRequestReceived(false);
                LOG.debug("Render task created: " + renderTask.toString());
                RenderTaskHistory renderTaskHistory = new RenderTaskHistory();
                renderTaskHistory.setComputeType(compute_type);
                renderTaskHistory.setEngine(blender_engine);
                renderTaskHistory.setFrameAndPartNumbers(frame_number + ":" + part_number);
                renderTaskHistory.setProjectName(project_name);
                renderTaskHistory.setServerName(sethlansServer.getHostname());
                renderTaskHistory.setTaskDate(System.currentTimeMillis());
                renderTaskHistory.setRenderTaskUUID(renderTask.getRenderTaskUUID());
                renderTaskHistory.setCompleted(false);
                renderTaskHistory.setFailed(false);
                renderTaskHistoryDatabaseService.saveOrUpdate(renderTaskHistory);
                LOG.info("Received a " + compute_type + " render task from " + sethlansServer.getHostname() + " for project " + project_name);
                LOG.info("Part " + part_number + " of Frame " + frame_number);
                renderTaskDatabaseService.saveOrUpdate(renderTask);
                blenderRenderService.startRender(renderTask.getServerQueueUUID());
            }
        }
    }

    private void rejectRequest(@RequestParam String connection_uuid, @RequestParam String queue_item_uuid) {
        LOG.info("All slots are currently full. Rejecting request");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
        String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_reject_item/";
        String params = "queue_item_uuid=" + queue_item_uuid;
        sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
    }

    @PostMapping(value = "/api/benchmark/request")
    public void benchmarkRequest(@RequestParam String connection_uuid, @RequestParam ComputeType compute_type, @RequestParam String blender_version) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.error("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            List<BlenderBenchmarkTask> blenderBenchmarkTaskList = benchmarkTaskDatabaseService.listAll();
            if (blenderBenchmarkTaskList.size() > 0) {
                LOG.debug("Clearing out any existing benchmarks associated with the requesting server");
                benchmarkTaskDatabaseService.deleteAllByConnection(connection_uuid);
            }
            String[] cudaList = cuda.split(",");
            BlenderBenchmarkTask cpuBenchmarkTask = new BlenderBenchmarkTask();
            cpuBenchmarkTask.setBlenderVersion(blender_version);
            cpuBenchmarkTask.setBenchmarkURL("bmw_cpu");
            cpuBenchmarkTask.setComputeType(ComputeType.CPU);
            cpuBenchmarkTask.setComplete(false);
            cpuBenchmarkTask.setConnectionUUID(connection_uuid);
            cpuBenchmarkTask.setBenchmarkUUID(SethlansQueryUtils.getShortUUID());

            List<BlenderBenchmarkTask> gpuTasks = new ArrayList<>();


            for (String cuda : cudaList) {
                BlenderBenchmarkTask gpuBenchmarkTask = new BlenderBenchmarkTask();
                gpuBenchmarkTask.setBlenderVersion(blender_version);
                gpuBenchmarkTask.setComplete(false);
                gpuBenchmarkTask.setDeviceID(cuda);
                gpuBenchmarkTask.setBenchmarkURL("bmw_gpu");
                gpuBenchmarkTask.setComputeType(ComputeType.GPU);
                gpuBenchmarkTask.setConnectionUUID(connection_uuid);
                gpuBenchmarkTask.setBenchmarkUUID(SethlansQueryUtils.getShortUUID());
                gpuTasks.add(gpuBenchmarkTask);
            }

            List<String> benchmarks = new ArrayList<>();
            switch (compute_type) {
                case CPU:
                    benchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                    blenderBenchmarkService.processReceivedBenchmark(cpuBenchmarkTask.getBenchmarkUUID());
                    break;
                case CPU_GPU:
                    benchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                    for (BlenderBenchmarkTask gpuTask : gpuTasks) {
                        benchmarkTaskDatabaseService.saveOrUpdate(gpuTask);
                        benchmarks.add(gpuTask.getBenchmarkUUID());
                    }
                    benchmarks.add(cpuBenchmarkTask.getBenchmarkUUID());
                    blenderBenchmarkService.processReceivedBenchmarks(benchmarks);
                    break;
                case GPU:
                    for (BlenderBenchmarkTask gpuTask : gpuTasks) {
                        benchmarkTaskDatabaseService.saveOrUpdate(gpuTask);
                        benchmarks.add(gpuTask.getBenchmarkUUID());
                    }
                    blenderBenchmarkService.processReceivedBenchmarks(benchmarks);
                    break;
            }

        }
    }


    @Autowired
    public void setRenderTaskDatabaseService(RenderTaskDatabaseService renderTaskDatabaseService) {
        this.renderTaskDatabaseService = renderTaskDatabaseService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setBenchmarkTaskDatabaseService(BenchmarkTaskDatabaseService benchmarkTaskDatabaseService) {
        this.benchmarkTaskDatabaseService = benchmarkTaskDatabaseService;
    }

    @Autowired
    public void setBlenderBenchmarkService(BlenderBenchmarkService blenderBenchmarkService) {
        this.blenderBenchmarkService = blenderBenchmarkService;
    }

    @Autowired
    public void setBlenderRenderService(BlenderRenderService blenderRenderService) {
        this.blenderRenderService = blenderRenderService;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setRenderTaskHistoryDatabaseService(RenderTaskHistoryDatabaseService renderTaskHistoryDatabaseService) {
        this.renderTaskHistoryDatabaseService = renderTaskHistoryDatabaseService;
    }
}
