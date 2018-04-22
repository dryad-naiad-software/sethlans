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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.blender.BlenderBenchmarkService;
import com.dryadandnaiad.sethlans.services.blender.BlenderRenderService;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
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
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private BlenderBenchmarkService blenderBenchmarkService;
    private BlenderRenderService blenderRenderService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeRenderController.class);

    @RequestMapping(value = "/api/render/request", method = RequestMethod.POST)
    public void renderRequest(@RequestParam String project_name, @RequestParam String connection_uuid, @RequestParam String project_uuid,
                              @RequestParam String queue_item_uuid,
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
            ComputeType computeType = ComputeType.valueOf(SethlansUtils.getProperty(SethlansConfigKeys.COMPUTE_METHOD.toString()));
            LOG.debug("Render Request Received, preparing render task.");
            List<BlenderRenderTask> blenderRenderTaskList = blenderRenderTaskDatabaseService.listAll();
            if (computeType == ComputeType.CPU_GPU && blenderRenderTaskList.size() == 2 || computeType != ComputeType.CPU_GPU && blenderRenderTaskList.size() == 1) {
                LOG.debug("All slots are currently full. Rejecting request");
                SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
                String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_reject_item";
                String params = "queue_uuid=" + queue_item_uuid;
                sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
            } else {
                SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
                String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/project/node_accept_item";
                String params = "queue_uuid=" + queue_item_uuid;
                sethlansAPIConnectionService.sendToRemoteGET(connectionURL, params);
                BlenderRenderTask blenderRenderTask;
                BlenderFramePart framePart = new BlenderFramePart();
                framePart.setFrameNumber(frame_number);
                framePart.setFileExtension(file_extension);
                framePart.setFrameFileName(frame_filename);
                framePart.setPartFilename(part_filename);
                framePart.setPartNumber(part_number);
                framePart.setPartPositionMinY(part_position_min_y);
                framePart.setPartPositionMaxY(part_position_max_y);

                // Create a new task
                blenderRenderTask = new BlenderRenderTask();
                blenderRenderTask.setProject_uuid(project_uuid);
                blenderRenderTask.setProjectName(project_name);
                blenderRenderTask.setServer_queue_uuid(queue_item_uuid);
                blenderRenderTask.setConnection_uuid(connection_uuid);
                blenderRenderTask.setRenderOutputFormat(render_output_format);
                blenderRenderTask.setSamples(samples);
                blenderRenderTask.setBlenderEngine(blender_engine);
                blenderRenderTask.setComputeType(compute_type);
                blenderRenderTask.setBlendFilename(blend_file);
                blenderRenderTask.setBlenderVersion(blender_version);
                blenderRenderTask.setBlenderFramePart(framePart);
                blenderRenderTask.setTaskResolutionX(part_resolution_x);
                blenderRenderTask.setTaskResolutionY(part_resolution_y);
                blenderRenderTask.setPartResPercentage(part_res_percentage);
                blenderRenderTask.setComplete(false);
                LOG.debug(blenderRenderTask.toString());
                blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);
                blenderRenderService.startRenderService(project_uuid);
            }
        }
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
            cpuBenchmarkTask.setBenchmark_uuid(SethlansUtils.getShortUUID());

            List<BlenderBenchmarkTask> gpuTasks = new ArrayList<>();


            for (String cuda : cudaList) {
                BlenderBenchmarkTask gpuBenchmarkTask = new BlenderBenchmarkTask();
                gpuBenchmarkTask.setBlenderVersion(blender_version);
                gpuBenchmarkTask.setComplete(false);
                gpuBenchmarkTask.setDeviceID(cuda);
                gpuBenchmarkTask.setBenchmarkURL("bmw_gpu");
                gpuBenchmarkTask.setComputeType(ComputeType.GPU);
                gpuBenchmarkTask.setConnection_uuid(connection_uuid);
                gpuBenchmarkTask.setBenchmark_uuid(SethlansUtils.getShortUUID());
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
    public void setBlenderRenderTaskDatabaseService(BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService) {
        this.blenderRenderTaskDatabaseService = blenderRenderTaskDatabaseService;
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
