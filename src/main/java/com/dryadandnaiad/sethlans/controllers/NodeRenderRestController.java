/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderTask;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.services.database.BlenderBenchmarkTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderTaskDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created Mario Estrella on 12/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
public class NodeRenderRestController {

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private BlenderRenderTaskDatabaseService blenderRenderTaskDatabaseService;
    private BlenderBenchmarkTaskDatabaseService blenderBenchmarkTaskDatabaseService;
    private static final Logger LOG = LoggerFactory.getLogger(NodeRenderRestController.class);

    @RequestMapping(value = "/api/render/request", method = RequestMethod.POST)
    public void renderRequest(@RequestParam String server_uuid, String project_uuid, RenderOutputFormat renderoutputformat,
                              int start_frame, int end_frame, int step_frame, int samples,
                              BlenderEngine blender_engine, int resolution_x, int resolution_y, int res_percentage, ComputeType compute_type,
                              String blend_file, String blender_version, int part) {
        if (sethlansServerDatabaseService.getByConnectionUUID(server_uuid) == null) {
            LOG.debug("The uuid sent: " + server_uuid + " is not present in the database");
        } else {
            BlenderRenderTask blenderRenderTask;
            if (blenderRenderTaskDatabaseService.getByProjectUUID(project_uuid) == null) {
                // Create a new task
                blenderRenderTask = new BlenderRenderTask();
                blenderRenderTask.setProject_uuid(project_uuid);
                blenderRenderTask.setConnection_uuid(server_uuid);
                blenderRenderTask.setRenderOutputFormat(renderoutputformat);
                blenderRenderTask.setStartFrame(start_frame);
                blenderRenderTask.setEndFrame(end_frame);
                blenderRenderTask.setStepFrame(step_frame);
                blenderRenderTask.setSamples(samples);
                blenderRenderTask.setBlenderEngine(blender_engine);
                blenderRenderTask.setResolutionX(resolution_x);
                blenderRenderTask.setResolutionY(resolution_y);
                blenderRenderTask.setResPercentage(res_percentage);
                blenderRenderTask.setComputeType(compute_type);
                blenderRenderTask.setBlendFilename(blend_file);
                blenderRenderTask.setBlenderVersion(blender_version);
                blenderRenderTask.setPart(part);
                blenderRenderTaskDatabaseService.saveOrUpdate(blenderRenderTask);
            } else {
                // Update existing task to process a new part and a new frame if necessary
                blenderRenderTask = blenderRenderTaskDatabaseService.getByProjectUUID(project_uuid);
                blenderRenderTask.setPart(part);
                blenderRenderTask.setStartFrame(start_frame);
                blenderRenderTask.setEndFrame(end_frame);
                blenderRenderTask.setStepFrame(step_frame);
            }
        }
    }

    @RequestMapping(value = "/api/benchmark/request", method = RequestMethod.POST)
    public void benchmarkRequest(@RequestParam String connection_uuid, ComputeType compute_type, String blender_version) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            BlenderBenchmarkTask cpuBenchmarkTask = new BlenderBenchmarkTask();
            cpuBenchmarkTask.setBlenderVersion(blender_version);
            cpuBenchmarkTask.setBenchmarkURL("bmw_cpu");
            cpuBenchmarkTask.setConnection_uuid(connection_uuid);

            BlenderBenchmarkTask gpuBenchmarkTask = new BlenderBenchmarkTask();
            gpuBenchmarkTask.setBlenderVersion(blender_version);
            gpuBenchmarkTask.setBenchmarkURL("bmw_gpu");
            gpuBenchmarkTask.setConnection_uuid(connection_uuid);
            switch (compute_type) {
                case CPU:
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                    break;
                case CPU_GPU:
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(gpuBenchmarkTask);
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(cpuBenchmarkTask);
                case GPU:
                    blenderBenchmarkTaskDatabaseService.saveOrUpdate(gpuBenchmarkTask);
            }
        }
    }

    @RequestMapping(value = "/api/render/status", method = RequestMethod.GET)
    public void renderStatus(@RequestParam String connection_uuid) {

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
}
