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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.forms.project.ProjectForm;
import com.dryadandnaiad.sethlans.forms.project.VideoChangeForm;
import com.dryadandnaiad.sethlans.services.blender.BlenderParseBlendFileService;
import com.dryadandnaiad.sethlans.services.blender.BlenderProjectService;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.queue.ProcessImageAndAnimationService;
import com.dryadandnaiad.sethlans.utils.SethlansFileUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.io.IOException;

import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.serveFile;
import static com.dryadandnaiad.sethlans.utils.SethlansQueryUtils.checkFrameRate;

/**
 * Created Mario Estrella on 9/11/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ProjectActionsController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectActionsController.class);
    private BlenderProjectService blenderProjectService;
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private BlenderParseBlendFileService blenderParseBlenderFileService;
    private ProcessImageAndAnimationService processImageAndAnimationService;
    @Value("${sethlans.benchmarkDir}")
    private String benchmarkDir;
    @Value("${sethlans.blenderDir}")
    private String blenderDir;
    @Value("${sethlans.projectDir}")
    private String projectDir;
    @Value("${sethlans.tempDir}")
    private String temp;

    @GetMapping(value = "/api/project_actions/delete_all_projects")
    public void deleteAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectService.deleteAllProjects();
        } else {
            blenderProjectService.deleteAllUserProjects(auth.getName());
        }

    }

    @GetMapping(value = "/api/project_actions/stop_project/{id}")
    public void stopProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectService.stopProject(id);
        } else {
            blenderProjectService.stopProject(auth.getName(), id);
        }
    }

    @GetMapping(value = "/api/project_actions/pause_project/{id}")
    public void pauseProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectService.pauseProject(id);
        } else {
            blenderProjectService.pauseProject(auth.getName(), id);
        }
    }

    @GetMapping(value = "/api/project_actions/resume_project/{id}")
    public void resumeProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectService.resumeProject(id);
        } else {
            blenderProjectService.pauseProject(auth.getName(), id);
        }
    }

    @GetMapping(value = "/api/project_actions/delete_project/{id}")
    public void deleteProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectService.deleteProject(id);
        } else {
            blenderProjectService.deleteProject(auth.getName(), id);
        }
    }

    @GetMapping(value = "/api/project_actions/download_project/{id}")
    public void downloadProject(@PathVariable Long id, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject project;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = blenderProjectDatabaseService.getById(id);
        } else {
            project = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        if (project.getProjectType().equals(ProjectType.STILL_IMAGE)) {
            LOG.debug(project.getFrameFileNames().size() + " " + project.toString());
            File image = new File(project.getFrameFileNames().get(0));
            serveFile(image, response);
        }
        if (project.getProjectType().equals(ProjectType.ANIMATION)) {
            File zipFile = SethlansFileUtils.createArchive(project.getFrameFileNames(), project.getProjectRootDir(), project.getProjectName().toLowerCase());
            if (zipFile != null) {
                serveFile(zipFile, response);
            }
        }
    }

    @GetMapping(value = "/api/project_actions/encode_project_video/{id}")
    public void encodeVideo(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject project;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = blenderProjectDatabaseService.getById(id);
        } else {
            project = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        switch (project.getRenderOutputFormat()) {
            case AVI:
                processImageAndAnimationService.createAVI(project);
                break;
            case MP4:
                processImageAndAnimationService.createMP4(project);
                break;
            case PNG:
                break;
        }

    }

    @GetMapping(value = "/api/project_actions/download_project_video/{id}")
    public void downloadVideo(@PathVariable Long id, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject project;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = blenderProjectDatabaseService.getById(id);
        } else {
            project = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        if (project.getProjectType().equals(ProjectType.ANIMATION) && project.getRenderOutputFormat().equals(RenderOutputFormat.MP4)
                || project.getRenderOutputFormat().equals(RenderOutputFormat.AVI)) {
            File video = new File(project.getMovieFileLocation());
            serveFile(video, response);
        }
    }

    @GetMapping(value = "/api/project_actions/start_project/{id}")
    public boolean startProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getById(id);
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        if (blenderProject == null) {
            return false;
        }
        blenderProjectService.startProject(blenderProject);
        return true;
    }

    @PostMapping(value = "/api/project_form/upload_project")
    public ProjectForm newProjectUpload(@RequestParam("projectFile") MultipartFile projectFile) {
        LOG.info(projectFile.getOriginalFilename().toLowerCase() + " uploading");
        String uploadTag = SethlansQueryUtils.getShortUUID();
        String filename = uploadTag + "-" + projectFile.getOriginalFilename().toLowerCase();
        File location = new File(temp + uploadTag + "_zip_file");
        try {
            if (projectFile.getContentType().contains("zip") || FilenameUtils.isExtension(projectFile.getOriginalFilename().toLowerCase(), "zip")) {
                location.mkdir();
                File storeUpload = new File(location + File.separator + uploadTag + "-" + projectFile.getOriginalFilename().toLowerCase());
                projectFile.transferTo(storeUpload);
            } else {
                File storeUpload = new File(temp + uploadTag + "-" + projectFile.getOriginalFilename().toLowerCase());
                projectFile.transferTo(storeUpload);
            }

        } catch (IOException e) {
            LOG.error("Error saving upload" + e.getMessage());
            LOG.debug(Throwables.getStackTraceAsString(e));
        }
        ProjectForm newProject = new ProjectForm();
        newProject.setUploadedFile(projectFile.getOriginalFilename().toLowerCase());

        if (projectFile.getContentType().contains("zip") || FilenameUtils.isExtension(projectFile.getOriginalFilename().toLowerCase(), "zip")) {
            String filenameWithoutExt = FilenameUtils.removeExtension(projectFile.getOriginalFilename().toLowerCase());
            newProject.setFileLocation(location + File.separator + uploadTag + "-" + projectFile.getOriginalFilename().toLowerCase());
            SethlansFileUtils.archiveExtract(filename, location, false);
            File[] files = location.listFiles();
            for (File file : files != null ? files : new File[0]) {
                if (file.toString().contains(filenameWithoutExt + ".blend")) {
                    newProject.populateForm(blenderParseBlenderFileService.parseBlendFile(file.toString()));
                }
            }

        } else {
            newProject.setFileLocation(temp + uploadTag + "-" + projectFile.getOriginalFilename().toLowerCase());
            newProject.populateForm(blenderParseBlenderFileService.parseBlendFile(newProject.getFileLocation()));
        }
        LOG.debug(newProject.toString());
        LOG.info("Upload successful");
        return newProject;
    }

    @PostMapping(value = "/api/project_form/submit_project")
    public long submitProject(@RequestBody ProjectForm projectForm) {
        if (projectForm != null) {
            LOG.info("Project Submitted" + projectForm);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (projectForm.getProjectType() == ProjectType.STILL_IMAGE) {
                projectForm.setOutputFormat(RenderOutputFormat.PNG);
                projectForm.setEndFrame(projectForm.getStartFrame());
                projectForm.setStepFrame(1);
            }
            if (projectForm.getBlenderEngine() == BlenderEngine.BLENDER_RENDER) {
                projectForm.setRenderOn(ComputeType.CPU);
                projectForm.setSamples(0);
            }
            partConfiguration(projectForm);
            projectForm.setFrameRate(checkFrameRate(projectForm.getFrameRate()));
            projectForm.setUsername(auth.getName());
            projectForm.setProjectStatus(ProjectStatus.Added);
            return blenderProjectDatabaseService.saveOrUpdateProjectForm(projectForm).getId();
        }
        return 0;
    }

    private int partsToTheNearestSquare(int parts) {
        double root = Math.sqrt(parts);
        int nearestPerfectSquareRoot = (int) Math.round(root);
        return nearestPerfectSquareRoot * nearestPerfectSquareRoot;
    }


    @PostMapping(value = "/api/project_form/edit_project/{id}")
    public boolean editProject(@RequestBody ProjectForm projectForm, @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getById(id);
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        if (projectForm != null && blenderProject != null) {
            LOG.info("Project Edited" + projectForm);
            blenderProject.setProjectName(projectForm.getProjectName());
            blenderProject.setBlenderVersion(projectForm.getSelectedBlenderversion());
            blenderProject.setRenderOutputFormat(projectForm.getOutputFormat());
            blenderProject.setProjectType(projectForm.getProjectType());
            blenderProject.setStartFrame(projectForm.getStartFrame());
            blenderProject.setEndFrame(projectForm.getEndFrame());
            blenderProject.setStepFrame(projectForm.getStepFrame());
            blenderProject.setRenderOn(projectForm.getRenderOn());
            blenderProject.setBlenderEngine(projectForm.getBlenderEngine());
            blenderProject.setSamples(projectForm.getSamples());
            blenderProject.setResolutionX(projectForm.getResolutionX());
            blenderProject.setResolutionY(projectForm.getResolutionY());
            blenderProject.setResPercentage(projectForm.getResPercentage());
            partConfiguration(projectForm);
            blenderProject.setFrameRate(projectForm.getFrameRate());
            blenderProjectDatabaseService.saveOrUpdate(blenderProject);
            return true;
        }
        return false;
    }

    private void partConfiguration(ProjectForm projectForm) {
        if (projectForm.isUseParts()) {
            if (projectForm.getPartsPerFrame() > 144) {
                projectForm.setPartsPerFrame(144);
            }
            if (projectForm.getPartsPerFrame() < 4) {
                projectForm.setPartsPerFrame(4);
            } else {
                projectForm.setPartsPerFrame(partsToTheNearestSquare(projectForm.getPartsPerFrame()));
            }
        }
        if (!projectForm.isUseParts()) {
            projectForm.setPartsPerFrame(1);
        }
    }

    @PostMapping(value = "/api/project_form/video_settings/{id}")
    public boolean editVideoSettings(@RequestBody VideoChangeForm videoChangeForm, @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getById(id);
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUser(auth.getName(), id);
        }
        if (videoChangeForm != null && blenderProject != null) {
            if (blenderProject.getProjectStatus().equals(ProjectStatus.Finished) && !videoChangeForm.getOutputFormat().equals(RenderOutputFormat.PNG)) {
                boolean changed = false;
                if (!blenderProject.getRenderOutputFormat().equals(videoChangeForm.getOutputFormat())) {
                    blenderProject.setRenderOutputFormat(videoChangeForm.getOutputFormat());
                    changed = true;
                }
                if (!blenderProject.getFrameRate().equals(videoChangeForm.getFrameRate())) {
                    blenderProject.setFrameRate(videoChangeForm.getFrameRate());
                    changed = true;
                }
                if (changed) {
                    LOG.info("Video Settings changed for " + blenderProject.getProjectName() + " : " + videoChangeForm);
                    blenderProject.setProjectStatus(ProjectStatus.Processing);
                    blenderProject.setReEncode(true);
                    blenderProject = blenderProjectDatabaseService.saveOrUpdate(blenderProject);
                    try {
                        LOG.info("Starting encoding process");
                        switch (blenderProject.getRenderOutputFormat()) {
                            case MP4:
                                FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir() + File.separator + "MP4"));
                                processImageAndAnimationService.createMP4(blenderProject);
                                break;
                            case AVI:
                                FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir() + File.separator + "AVI"));
                                processImageAndAnimationService.createAVI(blenderProject);
                                break;
                        }

                    } catch (Exception e) {
                        LOG.error("Exception found during encoding " + e.getMessage());
                        LOG.debug(Throwables.getStackTraceAsString(e));

                    }

                } else {
                    LOG.warn("Settings are the same, no change detected.");
                }
            }
        }
        return false;
    }

    @Autowired
    public void setBlenderProjectService(BlenderProjectService blenderProjectService) {
        this.blenderProjectService = blenderProjectService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderParseBlenderFileService(BlenderParseBlendFileService blenderParseBlenderFileService) {
        this.blenderParseBlenderFileService = blenderParseBlenderFileService;
    }

    @Autowired
    public void setProcessImageAndAnimationService(ProcessImageAndAnimationService processImageAndAnimationService) {
        this.processImageAndAnimationService = processImageAndAnimationService;
    }
}
