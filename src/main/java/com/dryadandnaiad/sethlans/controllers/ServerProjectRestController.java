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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderRenderQueueItem;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.blender.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ServerProjectRestController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerProjectRestController.class);

    @Value("${sethlans.benchmarkDir}")
    private String benchmarkDir;

    @Value("${sethlans.blenderDir}")
    private String blenderDir;

    @Value("${sethlans.projectDir}")
    private String projectDir;

    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private BlenderProjectService blenderProjectService;

    @RequestMapping(value = "/api/project/blender_binary", method = RequestMethod.GET)
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

    @RequestMapping(value = "/api/benchmark/response", method = RequestMethod.POST)
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
                    if (gpuDevice.getCudaName().equals(cuda_name)) {
                        gpuDevice.setRating(rating);
                        LOG.debug(sethlansNode.toString());
                    }
                }
            }
            sethlansNode.setBenchmarkComplete(complete);
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        }
    }

    @RequestMapping(value = "/api/benchmark_files/bmw_cpu", method = RequestMethod.GET)
    public void downloadCPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_cpu = new File(benchmarkDir + File.separator + "bmw27_cpu.blend");
            SethlansUtils.serveFile(bmw27_cpu, response);

        }

    }

    @RequestMapping(value = "/api/benchmark_files/bmw_gpu", method = RequestMethod.GET)
    public void downloadGPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_gpu = new File(benchmarkDir + File.separator + "bmw27_gpu.blend");
            SethlansUtils.serveFile(bmw27_gpu, response);
        }
    }

    @RequestMapping(value = "/api/project/response", method = RequestMethod.POST)
    public void projectResponse(@RequestParam String connection_uuid,
                                @RequestParam String project_uuid,
                                @RequestParam MultipartFile part,
                                @RequestParam int part_number,
                                @RequestParam int frame_number) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        }
        if (!part.isEmpty()) {
            try {
                String hostname = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid).getHostname();
                // For busy environments with lots of nodes this prevents the server from getting overwhelmed.
                Integer randomSleep;
                Random r = new Random();
                randomSleep = r.nextInt(15000 - 5000) + 5000;
                LOG.debug("Render Task received from " + hostname + " throttling for " + randomSleep + " milliseconds");
                Thread.sleep(randomSleep);

                File storedDir = null;
                // Additional check to avoid writing to the same project at the same time.
                LOG.debug("Checking to see if project is in use.");
                while (blenderProjectDatabaseService.isProjectDBEntryInUse(project_uuid)) {
                    randomSleep = r.nextInt(30000 - 5000) + 5000;
                    Thread.sleep(randomSleep);
                    LOG.debug("Project in use, sleeping for " + randomSleep);

                }
                LOG.debug("Project is not in use, processing render task from " + hostname);
                BlenderProject blenderProject = blenderProjectDatabaseService.restControllerGetProjectProxy(project_uuid);
                List<BlenderRenderQueueItem> blenderRenderQueueItemList = blenderRenderQueueDatabaseService.queueItemsByProjectUUID(project_uuid);
                int projectTotalQueue = blenderProject.getPartsPerFrame() * blenderProject.getTotalNumOfFrames();
                int remainingTotalQueue = projectTotalQueue;
                int remainingPartsForFrame = 0;
                for (BlenderRenderQueueItem blenderRenderQueueItem : blenderRenderQueueItemList) {
                    if (blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() == frame_number &&
                            blenderRenderQueueItem.getBlenderFramePart().getPartNumber() == part_number) {
                        blenderRenderQueueItem.setRendering(false);
                        blenderRenderQueueItem.setComplete(true);
                        blenderRenderQueueItem.setPaused(false);
                        blenderRenderQueueItem.getBlenderFramePart().setStoredDir(blenderProject.getProjectRootDir() +
                                File.separator + "frame_" + frame_number + File.separator);
                        storedDir = new File(blenderRenderQueueItem.getBlenderFramePart().getStoredDir());
                        storedDir.mkdirs();
                        try {
                            byte[] bytes = part.getBytes();
                            Path path = Paths.get(storedDir.toString() + File.separator +
                                    blenderRenderQueueItem.getBlenderFramePart().getPartFilename() + "." +
                                    blenderRenderQueueItem.getBlenderFramePart().getFileExtension());
                            Files.write(path, bytes);
                            SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
                            sethlansNode.setRendering(false);
                            LOG.debug("Processing completed render from " + sethlansNode.getHostname());
                            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (blenderRenderQueueItem.isComplete()) {
                        remainingTotalQueue--;
                    }
                    if (!blenderRenderQueueItem.isComplete() && blenderRenderQueueItem.getBlenderFramePart().getFrameNumber() == frame_number) {
                        remainingPartsForFrame++;
                    }
                }
                for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
                    if (blenderFramePart.getFrameNumber() == frame_number) {
                        blenderFramePart.setStoredDir(storedDir.toString() + File.separator);
                    }
                }
                LOG.debug("Remaining Parts per Frame for Frame " + frame_number + ": " + remainingPartsForFrame + " out of " + blenderProject.getPartsPerFrame());
                LOG.debug("Remaining Items in Queue: " + remainingTotalQueue);
                LOG.debug("Project Total Queue " + projectTotalQueue);
                double currentPercentage = ((projectTotalQueue - remainingTotalQueue) * 100.0) / projectTotalQueue;
                LOG.debug("Current Percentage " + currentPercentage);
                blenderProject.setCurrentPercentage((int) currentPercentage);
                if (remainingPartsForFrame == 0) {
                    if (blenderProjectService.combineParts(blenderProject, frame_number)) {
                        if (remainingTotalQueue == 0) {
                            blenderProject.setFinished(true);
                            blenderProject.setAllImagesProcessed(true);
                        }
                    }
                }
                blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequestMapping(value = "/api/project/blend_file/", method = RequestMethod.GET)
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
    public void setBlenderRenderQueueDatabaseService(BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService) {
        this.blenderRenderQueueDatabaseService = blenderRenderQueueDatabaseService;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }
}
