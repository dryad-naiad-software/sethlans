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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.blender.BlenderProcessRenderQueueService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ProjectCommunicationController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectCommunicationController.class);

    @Value("${sethlans.benchmarkDir}")
    private String benchmarkDir;

    @Value("${sethlans.blenderDir}")
    private String blenderDir;

    @Value("${sethlans.projectDir}")
    private String projectDir;

    @Value("${sethlans.tempDir}")
    private String temp;


    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderProcessRenderQueueService blenderProcessRenderQueueService;


    @GetMapping(value = "/api/project/blender_binary")
    public void downloadBlenderBinary(HttpServletResponse response, @RequestParam String connection_uuid,
                                      @RequestParam String version, @RequestParam String os) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File dir = new File(blenderDir + File.separator + "binaries" + File.separator + version);
            FileFilter fileFilter = new WildcardFileFilter(version + "-" + os.toLowerCase() + "." + "*");
            File[] files = dir.listFiles(fileFilter);
            if (files != null) {
                if (files.length > 1) {
                    LOG.error("More files than expected, only one archive per os + version expected");
                } else {
                    File blenderBinary = files[0];
                    SethlansUtils.serveFile(blenderBinary, response);
                }
            } else {
                LOG.error("No files found.");
            }
        }
    }

    @PostMapping(value = "/api/benchmark/response")
    public void benchmarkResponse(@RequestParam String connection_uuid, @RequestParam int rating, @RequestParam String cuda_name,
                                  @RequestParam ComputeType compute_type, @RequestParam boolean complete) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
        if (sethlansNode == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            LOG.debug("Receiving benchmark from Node: " + sethlansNode.getHostname());
            if (compute_type.equals(ComputeType.CPU)) {
                sethlansNode.setCpuRating(rating);
            }
            if (compute_type.equals(ComputeType.GPU)) {
                for (GPUDevice gpuDevice : sethlansNode.getSelectedGPUs()) {
                    if (gpuDevice.getDeviceID().equals(cuda_name)) {
                        gpuDevice.setRating(rating);
                        LOG.debug(sethlansNode.toString());
                    }
                }
            }
            sethlansNode.setBenchmarkComplete(complete);
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        }
    }

    @GetMapping(value = "/api/benchmark_files/bmw_cpu")
    public void downloadCPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_cpu = new File(benchmarkDir + File.separator + "bmw27_cpu.blend");
            SethlansUtils.serveFile(bmw27_cpu, response);

        }

    }

    @GetMapping(value = "/api/benchmark_files/bmw_gpu")
    public void downloadGPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_gpu = new File(benchmarkDir + File.separator + "bmw27_gpu.blend");
            SethlansUtils.serveFile(bmw27_gpu, response);
        }
    }

    @PostMapping(value = "/api/project/response")
    public void projectResponse(@RequestParam String connection_uuid,
                                @RequestParam String project_uuid,
                                @RequestParam MultipartFile part,
                                @RequestParam int part_number,
                                @RequestParam int frame_number) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            if (!part.isEmpty()) {
                BlenderProcessQueueItem blenderProcessQueueItem = new BlenderProcessQueueItem();
                blenderProcessQueueItem.setConnection_uuid(connection_uuid);
                blenderProcessQueueItem.setProject_uuid(project_uuid);
                blenderProcessQueueItem.setPart(part);
                blenderProcessQueueItem.setPart_number(part_number);
                blenderProcessQueueItem.setFrame_number(frame_number);
                blenderProcessRenderQueueService.addQueueItem(blenderProcessQueueItem);


            }
        }

    }

    @GetMapping(value = "/api/project/blend_file/")
    public void downloadBlendfile(HttpServletResponse response, @RequestParam String connection_uuid, @RequestParam String project_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUID(project_uuid);
            File blend_file = new File(blenderProject.getBlendFileLocation());
            SethlansUtils.serveFile(blend_file, response);
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
    public void setBlenderProcessRenderQueueService(BlenderProcessRenderQueueService blenderProcessRenderQueueService) {
        this.blenderProcessRenderQueueService = blenderProcessRenderQueueService;
    }
}
