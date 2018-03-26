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
import com.dryadandnaiad.sethlans.domains.info.ProjectInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.blender.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.BlenderRenderQueueDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.storage.WebUploadService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

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
    private BlenderRenderQueueDatabaseService blenderRenderQueueDatabaseService;
    private BlenderProjectService blenderProjectService;
    private WebUploadService webUploadService;
    private BlenderParseBlendFileService blenderParseBlenderFileService;

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
                blenderProjectDatabaseService.releaseObject(project_uuid);
            } catch (InterruptedException e) {
                LOG.debug("Shutting down Project Rest Controller");
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

    @GetMapping(value = "/api/project_ui/num_of_projects")
    public Integer numberOfProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return blenderProjectDatabaseService.listAllReverse().size();
        } else {
            return blenderProjectDatabaseService.getProjectsByUser(auth.getName()).size();
        }
    }

    @GetMapping(value = "/api/project_ui/nodes_ready")
    public boolean nodesReady() {
        return sethlansNodeDatabaseService.activeNodes();
    }

    @GetMapping(value = "/api/project_ui/project_list")
    public List<ProjectInfo> getProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.listAllReverse());
        } else {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getProjectsByUser(auth.getName()));
        }

    }

    @PostMapping(value = "/api/project_form/upload_project")
    public ProjectForm newProjectUpload(@RequestParam("projectFile") MultipartFile projectFile) {
        LOG.debug("Upload Attempted");
        String uploadTag = SethlansUtils.getShortUUID();
        webUploadService.store(projectFile, uploadTag);
        ProjectForm newProject = new ProjectForm();
        newProject.setUploadedFile(projectFile.getOriginalFilename());
        newProject.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename());
        newProject.populateForm(blenderParseBlenderFileService.parseBlendFile(newProject.getFileLocation()));
        LOG.debug(newProject.toString());
        return newProject;
    }

    @PostMapping(value = "/api/project_form/submit_project")
    public boolean submitProject(@RequestBody ProjectForm projectForm) {
        if (projectForm != null) {
            LOG.debug("Project Submitted" + projectForm);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            projectForm.setUsername(auth.getName());
            blenderProjectDatabaseService.saveOrUpdateProjectForm(projectForm);
            return true;
        }
        return false;
    }

    private List<ProjectInfo> convertBlenderProjectToProjectInfo(List<BlenderProject> projectsToConvert) {
        List<ProjectInfo> projectsToReturn = new ArrayList<>();
        for (BlenderProject blenderProject : projectsToConvert) {
            ProjectInfo projectInfo = new ProjectInfo();
            projectInfo.setId(blenderProject.getId());
            projectInfo.setProjectType(blenderProject.getProjectType());
            projectInfo.setProjectName(blenderProject.getProjectName());
            projectInfo.setSelectedBlenderversion(blenderProject.getBlenderVersion());
            projectInfo.setRenderOn(blenderProject.getRenderOn());
            projectInfo.setUsername(blenderProject.getSethlansUser().getUsername());
            projectInfo.setResolutionX(blenderProject.getResolutionX());
            projectInfo.setResolutionY(blenderProject.getResolutionY());
            projectsToReturn.add(projectInfo);
        }
        return projectsToReturn;
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

    @Autowired
    public void setWebUploadService(WebUploadService webUploadService) {
        this.webUploadService = webUploadService;
    }

    @Autowired
    public void setBlenderParseBlenderFileService(BlenderParseBlendFileService blenderParseBlenderFileService) {
        this.blenderParseBlenderFileService = blenderParseBlenderFileService;
    }
}
