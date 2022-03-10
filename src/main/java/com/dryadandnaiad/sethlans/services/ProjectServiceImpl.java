/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.converters.ProjectFormToProject;
import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.BlendFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.project.ImageSettings;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectSettings;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File created by Mario Estrella on 12/27/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Profile({"SERVER", "DUAL"})
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BlenderArchiveRepository blenderArchiveRepository;
    private final ProjectFormToProject projectFormToProject;
    private final ServerQueueService serverQueueService;
    private final NodeRepository nodeRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository,
                              BlenderArchiveRepository blenderArchiveRepository,
                              ProjectFormToProject projectFormToProject, ServerQueueService serverQueueService,
                              NodeRepository nodeRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.projectFormToProject = projectFormToProject;
        this.serverQueueService = serverQueueService;
        this.nodeRepository = nodeRepository;
    }

    @Override
    public boolean startProject(String projectID) {
        if (nodeRepository.existsNodeByActiveIsTrue()) {
            if (projectRepository.getProjectByProjectID(projectID).isPresent()) {
                var project = projectRepository.getProjectByProjectID(projectID).get();
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
                    serverQueueService.addRenderTasksToServerQueue(project);
                } else {
                    if (project.getUser().getUsername().equals(auth.getName())) {
                        serverQueueService.addRenderTasksToServerQueue(project);
                    }
                }
                return true;
            }

        } else {
            return false;
        }
        return false;
    }

    @Override
    public boolean resumeProject(String projectID) {
        return false;
    }

    @Override
    public boolean pauseProject(String projectID) {
        return false;
    }

    @Override
    public boolean stopProject(String projectID) {
        return false;
    }

    @Override
    public void deleteProject(String projectID) {
        if (projectRepository.getProjectByProjectID(projectID).isPresent()) {
            var project = projectRepository.getProjectByProjectID(projectID).get();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
                projectRepository.delete(project);
            } else {
                if (project.getUser().getUsername().equals(auth.getName())) {
                    projectRepository.delete(project);
                }
            }
        } else {
            log.info("Unable to delete, there is no project with project id: " + projectID);
        }
    }

    @Override
    public void deleteAllProjectsByUser(String userID) {
        if (userRepository.findUserByUserID(userID).isPresent()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
                projectRepository.deleteAllByUser(userRepository.findUserByUserID(userID).get());
            } else {
                if (userRepository.findUserByUserID(userID).get().getUsername().equals(auth.getName())) {
                    projectRepository.deleteAllByUser(userRepository.findUserByUserID(userID).get());
                }
            }
        }
    }

    @Override
    public void deleteAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            projectRepository.deleteAll();
        } else {
            if (userRepository.findUserByUsername(auth.getName()).isPresent())
                projectRepository.deleteAllByUser(userRepository.findUserByUsername(auth.getName()).get());
        }
    }

    @Override
    public boolean projectExists(String projectID) {
        return projectRepository.getProjectByProjectID(projectID).isPresent();
    }

    @Override
    public ResponseEntity<ProjectForm> projectFileUpload(MultipartFile projectFile) {
        var originalFilename = Objects.requireNonNull(projectFile.getOriginalFilename()).toLowerCase();
        var uploadTag = QueryUtils.getShortUUID();
        log.info(originalFilename + " uploaded.");
        var filename = uploadTag + "-" + originalFilename;
        var tempDir = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR);
        var zipLocation = new File(tempDir + File.separator + uploadTag + "_zip_file");
        var filenameSplit = Arrays.asList(originalFilename.split("\\.(?=[^.]+$)"));
        log.debug("Filename and Extension: " + filenameSplit);
        var projectForm = ProjectForm.builder().originalFile(originalFilename).build();
        var blendFile = new BlendFile();
        var blendFilename = "";

        try {
            if (projectFile.isEmpty()) {
                throw new IOException(filename + " is empty!");
            }
            var stream = projectFile.getInputStream();
            if (Objects.requireNonNull(projectFile.getContentType()).contains("zip") ||
                    filenameSplit.get(1).contains("zip")) {
                zipLocation.mkdir();
                var storeUpload = new File(zipLocation + File.separator + filename);
                log.debug("Zip will be extracted and files will be stored here: " + storeUpload);
                Files.copy(stream, Paths.get(storeUpload.toString()), StandardCopyOption.REPLACE_EXISTING);
                var filenameWithoutExt = FilenameUtils.removeExtension(
                        originalFilename);
                projectForm.setProjectFileLocation(zipLocation + File.separator + filename);
                var blendFileNotFound = true;
                if (FileUtils.extractArchive(storeUpload.toString(), zipLocation.toString(), false)) {
                    var files = zipLocation.listFiles();
                    for (File file : files) {
                        log.info(file.toString());
                        log.info(filenameWithoutExt);
                        if (file.toString().contains(".blend")) {
                            blendFile = BlenderUtils.parseBlendFile(file.toString(),
                                    ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR),
                                    ConfigUtils.getProperty(ConfigKeys.PYTHON_DIR));
                            if (blendFile == null) {
                                throw new IOException("Unable to read blend file");
                            }
                            blendFileNotFound = false;
                            blendFilename = file.toString();
                            break;
                        }
                    }
                    if (blendFileNotFound) {
                        throw new IOException(originalFilename + " does not contain a blend file!");
                    }
                } else {
                    throw new IOException(originalFilename + " is not a valid archive.");
                }


            } else {
                if (!filenameSplit.get(1).contains("blend")) {
                    throw new IOException("This is not a valid blend file: " + originalFilename);
                }
                blendFilename = originalFilename;
                var storeUpload = new File(tempDir + File.separator + filename);
                log.debug("Upload will be stored here: " + storeUpload);
                Files.copy(stream, Paths.get(storeUpload.toString()), StandardCopyOption.REPLACE_EXISTING);
                projectForm.setProjectFileLocation(tempDir + File.separator + filename);
                blendFile = BlenderUtils.parseBlendFile(projectForm.getProjectFileLocation(),
                        ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR),
                        ConfigUtils.getProperty(ConfigKeys.PYTHON_DIR));
                if (blendFile == null) {
                    throw new IOException("Unable to read blend file");
                }
            }
            var imageSettings = ImageSettings.builder()
                    .resolutionX(blendFile.getResolutionX())
                    .resolutionY(blendFile.getResolutionY())
                    .resPercentage(blendFile.getResPercent())
                    .imageOutputFormat(ImageOutputFormat.PNG)
                    .build();

            var blenderArchiveList = blenderArchiveRepository.findAllByDownloadedIsTrue();
            var versions = new ArrayList<String>();
            for (BlenderArchive blenderArchive : blenderArchiveList) {
                versions.add(blenderArchive.getBlenderVersion());
            }
            versions.sort(new AlphaNumericComparator());
            Collections.reverse(versions);
            log.info(blendFilename);


            var projectSettings = ProjectSettings.builder()
                    .samples(50)
                    .partsPerFrame(4)
                    .useParts(true)
                    .computeOn(ComputeOn.HYBRID)
                    .startFrame(blendFile.getFrameStart())
                    .endFrame(blendFile.getFrameEnd())
                    .stepFrame(blendFile.getFrameSkip())
                    .imageSettings(imageSettings)
                    .blenderEngine(blendFile.getEngine())
                    .blenderVersion(versions.get(0))
                    .blendFilename(blendFilename)
                    .build();

            projectForm.setProjectSettings(projectSettings);
            projectForm.setProjectType(ProjectType.STILL_IMAGE);
            projectForm.setProjectID(UUID.randomUUID().toString());
            projectForm.setProjectName("");
            log.info(projectForm.toString());

            return new ResponseEntity<>(projectForm, HttpStatus.CREATED);
        } catch (IOException | UnsupportedOperationException e) {
            log.error("Error saving upload: " + e.getMessage());
            log.debug(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


    }

    @Override
    public boolean createProject(ProjectForm projectForm) {
        if (projectForm.getProjectName().isEmpty() || projectForm.getProjectName().isBlank()) {
            return false;
        }
        switch (projectForm.getProjectType()) {
            case ANIMATION -> {
                if (projectForm.getProjectSettings().getAnimationType() == AnimationType.MOVIE && projectForm.getProjectSettings().getVideoSettings() == null) {
                    return false;
                }
            }
            case STILL_IMAGE -> {
                if (projectForm.getProjectSettings().getImageSettings() == null) {
                    return false;
                }
            }
        }
        try {
            var projectDirectory = new File(ConfigUtils.getProperty(ConfigKeys.PROJECT_DIR) + File.separator + projectForm.getProjectID());
            if (projectForm.getProjectFileLocation().contains(".zip")) {
                org.apache.commons.io.FileUtils.moveFile(new File(projectForm.getProjectFileLocation()), new File(projectDirectory + File.separator + projectForm.getOriginalFile()));
                projectForm.setProjectFileLocation(projectDirectory + File.separator + projectForm.getOriginalFile());
                projectForm.getProjectSettings().setBlendFilenameMD5Sum(FileUtils.getMD5ofFile(new File(projectForm.getProjectFileLocation())));

            } else {
                org.apache.commons.io.FileUtils.moveFile(new File(projectForm.getProjectFileLocation()), new File(projectDirectory + File.separator + projectForm.getProjectSettings().getBlendFilename()));
                projectForm.setProjectFileLocation(projectDirectory + File.separator + projectForm.getProjectSettings().getBlendFilename());
                projectForm.getProjectSettings().setBlendFilenameMD5Sum(FileUtils.getMD5ofFile(new File(projectDirectory + File.separator + projectForm.getProjectSettings().getBlendFilename())));

            }

            var project = projectFormToProject.convert(projectForm);
            project.getProjectStatus().setQueueIndex(0);
            if (project.getProjectType() == ProjectType.STILL_IMAGE) {
                project.getProjectSettings().setEndFrame(project.getProjectSettings().getStartFrame());
                project.getProjectSettings().setTotalNumberOfFrames(1);
                if (project.getProjectSettings().isUseParts()) {
                    project.getProjectStatus().setTotalQueueSize(project.getProjectSettings().getPartsPerFrame());
                }
            } else {
                var startFrame = project.getProjectSettings().getStartFrame();
                var endFrame = project.getProjectSettings().getEndFrame();
                var step = project.getProjectSettings().getStepFrame();
                int totalFrames = 0;
                if (startFrame == 1) {
                    totalFrames = endFrame / step;
                } else {
                    for (var i = startFrame; i < endFrame; i += step) {
                        totalFrames++;
                    }
                }

                project.getProjectSettings().setTotalNumberOfFrames(totalFrames);
                if (project.getProjectSettings().isUseParts()) {
                    project.getProjectStatus()
                            .setTotalQueueSize(totalFrames * project.getProjectSettings().getPartsPerFrame());

                } else {
                    project.getProjectStatus()
                            .setTotalQueueSize(totalFrames);
                }

            }
            project.getProjectStatus().setRemainingQueueSize(project.getProjectStatus().getTotalQueueSize());

            project.setProjectRootDir(projectDirectory.toString());
            projectRepository.save(project);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
