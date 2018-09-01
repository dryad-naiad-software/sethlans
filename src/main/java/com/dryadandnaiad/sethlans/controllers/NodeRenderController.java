/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.domains.database.queue.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.blender.BlenderRenderService;
import com.dryadandnaiad.sethlans.services.blender.benchmark.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private BlenderRenderService blenderRenderService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeRenderController.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @RequestMapping(value = "/api/render/request", method = RequestMethod.POST)
    public void renderRequest(@RequestParam String project_name, @RequestParam String connection_uuid, @RequestParam String project_uuid,
                              @RequestParam String queue_item_uuid, @RequestParam String gpu_device_id,
                              @RequestParam RenderOutputFormat render_output_format,
                              @RequestParam int samples, @RequestParam BlenderEngine blender_engine, @RequestParam ComputeType compute_type,
                              @RequestParam String blend_file, @RequestParam String blender_version,
                              @RequestParam String frame_filename, @RequestParam int frame_number, @RequestParam int part_number,
                              @RequestParam int part_resolution_x, @RequestParam int part_resolution_y,
                              @RequestParam double part_position_min_y, @RequestParam double part_position_max_y,
                              @RequestParam int part_res_percentage, @RequestParam String part_filename, @RequestParam String file_extension) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            ComputeType computeType = ComputeType.valueOf(getProperty(SethlansConfigKeys.COMPUTE_METHOD.toString()));
            NodeInfo nodeInfo = SethlansNodeUtils.getNodeInfo();
            LOG.debug("Render Request Received, preparing render task.");
            List<RenderTask> renderTaskList = renderTaskDatabaseService.listAll();
            boolean rejected = false;
            switch (computeType) {
                case CPU_GPU:
                    if (gpu_device_id == null || gpu_device_id.equals("null")) {
                        rejectRequest(connection_uuid, queue_item_uuid);
                        rejected = true;
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
                    if (gpu_device_id == null || gpu_device_id.equals("null")) {
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
                    if (renderTaskList.size() == (nodeInfo.getSelectedGPUs().size() + 1)) {
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
                sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
                RenderTask renderTask;
                BlenderFramePart framePart = new BlenderFramePart();
                framePart.setFrameNumber(frame_number);
                framePart.setFileExtension(file_extension);
                framePart.setFrameFileName(frame_filename);
                framePart.setPartFilename(part_filename);
                framePart.setPartNumber(part_number);
                framePart.setPartPositionMinY(part_position_min_y);
                framePart.setPartPositionMaxY(part_position_max_y);

                // Create a new task
                renderTask = new RenderTask();
                renderTask.setProject_uuid(project_uuid);
                renderTask.setProjectName(project_name);
                renderTask.setServer_queue_uuid(queue_item_uuid);
                renderTask.setConnection_uuid(connection_uuid);
                renderTask.setRenderOutputFormat(render_output_format);
                renderTask.setSamples(samples);
                renderTask.setBlenderEngine(blender_engine);
                renderTask.setComputeType(compute_type);
                renderTask.setBlendFilename(blend_file);
                renderTask.setBlenderVersion(blender_version);
                renderTask.setBlenderFramePart(framePart);
                renderTask.setTaskResolutionX(part_resolution_x);
                renderTask.setTaskResolutionY(part_resolution_y);
                renderTask.setPartResPercentage(part_res_percentage);
                renderTask.setComplete(false);

                if (compute_type == ComputeType.GPU) {
                    if (nodeInfo.isCombined()) {
                        renderTask.setDeviceID("COMBO");
                    } else {
                        renderTask.setDeviceID(gpu_device_id);
                    }
                } else {
                    renderTask.setDeviceID("CPU");
                }
                LOG.debug(renderTask.toString());
                renderTaskDatabaseService.saveOrUpdate(renderTask);
                blenderRenderService.startRender(renderTask.getServer_queue_uuid());
            }
        }
    }

    private void rejectRequest(@RequestParam String connection_uuid, @RequestParam String queue_item_uuid) {
        LOG.debug("All slots are currently full. Rejecting request");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
        String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_reject_item/";
        String params = "queue_item_uuid=" + queue_item_uuid;
        sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
    }

    @RequestMapping(value = "/api/benchmark/request", method = RequestMethod.POST)
    public void benchmarkRequest(@RequestParam String connection_uuid, @RequestParam ComputeType compute_type, @RequestParam String blender_version) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            List<BlenderBenchmarkTask> blenderBenchmarkTaskList = blenderBenchmarkTaskDatabaseService.listAll();
            if (blenderBenchmarkTaskList.size() > 0) {
                LOG.debug("Clearing out any existing benchmarks associated with the requesting server");
                blenderBenchmarkTaskDatabaseService.deleteAllByConnection(connection_uuid);
            }
            String[] cudaList = cuda.split(",");
            BlenderBenchmarkTask cpuBenchmarkTask = new BlenderBenchmarkTask();
            cpuBenchmarkTask.setBlenderVersion(blender_version);
            cpuBenchmarkTask.setBenchmarkURL("bmw_cpu");
            cpuBenchmarkTask.setComputeType(ComputeType.CPU);
            cpuBenchmarkTask.setComplete(false);
            cpuBenchmarkTask.setConnection_uuid(connection_uuid);
            cpuBenchmarkTask.setBenchmark_uuid(SethlansQueryUtils.getShortUUID());

            List<BlenderBenchmarkTask> gpuTasks = new ArrayList<>();


            for (String cuda : cudaList) {
                BlenderBenchmarkTask gpuBenchmarkTask = new BlenderBenchmarkTask();
                gpuBenchmarkTask.setBlenderVersion(blender_version);
                gpuBenchmarkTask.setComplete(false);
                gpuBenchmarkTask.setDeviceID(cuda);
                gpuBenchmarkTask.setBenchmarkURL("bmw_gpu");
                gpuBenchmarkTask.setComputeType(ComputeType.GPU);
                gpuBenchmarkTask.setConnection_uuid(connection_uuid);
                gpuBenchmarkTask.setBenchmark_uuid(SethlansQueryUtils.getShortUUID());
                gpuTasks.add(gpuBenchmarkTask);
            }

            List<String> benchmarks = new ArrayList<>();
            switch (compute_type) {
                case CPU:
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                    blenderBenchmarkService.processReceivedBenchmark(cpuBenchmarkTask.getBenchmark_uuid());
                    break;
                case CPU_GPU:
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                    for (BlenderBenchmarkTask gpuTask : gpuTasks) {
                        blenderBenchmarkTaskDatabaseService.saveOrUpdate(gpuTask);
                        benchmarks.add(gpuTask.getBenchmark_uuid());
                    }
                    benchmarks.add(cpuBenchmarkTask.getBenchmark_uuid());
                    blenderBenchmarkService.processReceivedBenchmarks(benchmarks);
                    break;
                case GPU:
                    for (BlenderBenchmarkTask gpuTask : gpuTasks) {
                        blenderBenchmarkTaskDatabaseService.saveOrUpdate(gpuTask);
                        benchmarks.add(gpuTask.getBenchmark_uuid());
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
    public void setBlenderBenchmarkTaskDatabaseService(BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService) {
        this.blenderBenchmarkTaskDatabaseService = blenderBenchmarkTaskDatabaseService;
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
}
