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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import com.google.common.base.Throwables;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
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
import java.io.IOException;

import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.serveFile;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ServerRenderController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerRenderController.class);

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
    private QueueService queueService;
    private SethlansNotificationService sethlansNotificationService;

    @GetMapping(value = "/api/project/blender_binary")
    public void downloadBlenderBinary(HttpServletResponse response, @RequestParam String connection_uuid,
                                      @RequestParam String version, @RequestParam String os) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File dir = new File(blenderDir + File.separator + "binaries" + File.separator + version);
            FileFilter fileFilter = new WildcardFileFilter(version + "-" + os.toLowerCase() + "." + "*");
            File[] files = dir.listFiles(fileFilter);
            if (files != null) {
                if (files.length > 1) {
                    LOG.error("More files than expected, only one archive per os + version expected");
                } else {
                    try {
                        File blenderBinary = files[0];
                        serveFile(blenderBinary, response);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        LOG.error(e.getMessage());
                        LOG.error(dir.toString() + " directory size is equal to " + files.length);

                    }
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
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            LOG.info("Receiving benchmark from Node: " + sethlansNode.getHostname());
            LOG.debug(sethlansNode.toString());
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
            String message = "Received " + compute_type + " benchmark result from " + sethlansNode.getHostname();
            SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.NODE, message);
            sethlansNotificationService.sendNotification(sethlansNotification);
            if (complete) {
                message = "All benchmarks complete for " + sethlansNode.getHostname() + ", marking node Active";
                sethlansNotification = new SethlansNotification(NotificationType.NODE, message);
                sethlansNotification.setLinkPresent(true);
                sethlansNotification.setMessageLink("/admin/nodes");
                sethlansNotification.setMailable(true);
                sethlansNotification.setSubject("Benchmark complete for " + sethlansNode.getHostname());
                sethlansNotificationService.sendNotification(sethlansNotification);
            }
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        }
    }

    @GetMapping(value = "/api/benchmark_files/bmw_cpu")
    public void downloadCPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_cpu = new File(benchmarkDir + File.separator + "bmw27_cpu.blend");
            serveFile(bmw27_cpu, response);

        }

    }

    @GetMapping(value = "/api/benchmark_files/bmw_gpu")
    public void downloadGPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_gpu = new File(benchmarkDir + File.separator + "bmw27_gpu.blend");
            serveFile(bmw27_gpu, response);
        }
    }

    @PostMapping(value = "/api/project/response")
    public void projectResponse(@RequestParam String connection_uuid,
                                @RequestParam MultipartFile part,
                                @RequestParam String queue_uuid, @RequestParam String project_uuid, @RequestParam long render_time) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
            if (blenderProjectDatabaseService.getByProjectUUID(project_uuid) == null) {
                LOG.info("Project does not exist. Discarding response.");
            }
        } else {
            if (!part.isEmpty()) {
                SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
                BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUIDWithoutFrameParts(project_uuid);
                if (blenderProject != null) {
                    if (!blenderProject.isUserStopped()) {
                        LOG.info("Received response from " + sethlansNode.getHostname() + ", adding to processing Queue.");
                        try {
                            File receivedFile = new File(blenderProject.getProjectRootDir() + File.separator + "received" + File.separator + StringUtils.left(queue_uuid, 8) + "-" + System.currentTimeMillis() + ".png");
                            part.transferTo(receivedFile);
                            ProcessQueueItem processQueueItem = new ProcessQueueItem();
                            processQueueItem.setConnectionUUID(connection_uuid);
                            processQueueItem.setPart(receivedFile.toString());
                            LOG.info("Received file from " + sethlansNode.getHostname() + " saved as " + receivedFile.toString());
                            processQueueItem.setQueueUUID(queue_uuid);
                            processQueueItem.setProjectUUID(project_uuid);
                            processQueueItem.setRenderTime(render_time);
                            queueService.addItemToProcess(processQueueItem);
                        } catch (IOException e) {
                            LOG.error(Throwables.getStackTraceAsString(e));
                        }
                    } else {
                        LOG.info("Project stopped, discarding response");
                    }
                } else {
                    LOG.info("Project does not exist.");
                }
                
                
            }
        }
    }

    @PostMapping(value = "/api/project/node_accept_item/")
    public void acceptedQueueItem(@RequestParam String queue_item_uuid) {
        queueService.nodeAcknowledgeQueueItem(queue_item_uuid);
    }

    @PostMapping(value = "/api/project/node_reject_item/")
    public void rejectedQueueItem(@RequestParam String queue_item_uuid) {
        queueService.nodeRejectQueueItem(queue_item_uuid);
    }

    @PostMapping(value = "/api/project/node_slot_update")
    public void nodeSlotUpdate(@RequestParam String connection_uuid, @RequestParam String device_id, @RequestParam int available_slots) {
        queueService.nodeSlotUpdate(connection_uuid, device_id, available_slots);
    }

    @GetMapping(value = "/api/project/blend_file/")
    public void downloadBlendfile(HttpServletResponse response, @RequestParam String connection_uuid, @RequestParam String project_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.info("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            BlenderProject blenderProject = blenderProjectDatabaseService.getByProjectUUIDWithoutFrameParts(project_uuid);
            File blend_file = new File(blenderProject.getBlendFileLocation());
            serveFile(blend_file, response);
        }

    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
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
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}
