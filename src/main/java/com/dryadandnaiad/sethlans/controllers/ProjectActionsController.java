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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.models.blender.BlendFile;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.project.ImageSettings;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectSettings;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.services.ProjectService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
@RequestMapping("/api/v1/project")
@Slf4j
public class ProjectActionsController {
    private final ProjectService projectService;
    private final BlenderArchiveRepository blenderArchiveRepository;

    public ProjectActionsController(ProjectService projectService, BlenderArchiveRepository blenderArchiveRepository) {
        this.projectService = projectService;
        this.blenderArchiveRepository = blenderArchiveRepository;
    }

    @DeleteMapping("/delete_project/{project_id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable("project_id") String projectID) {
        projectService.deleteProject(projectID);
    }


    @DeleteMapping("/delete_all_projects")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllProjects() {
        projectService.deleteAllProjects();
    }

    @PostMapping("/upload_project_file")
    public ResponseEntity<ProjectForm> newProjectUpload(@RequestParam("project_file") MultipartFile projectFile) {
        var originalFilename = Objects.requireNonNull(projectFile.getOriginalFilename()).toLowerCase();
        var uploadTag = QueryUtils.getShortUUID();
        log.info(originalFilename + " uploaded.");
        var filename = uploadTag + "-" + originalFilename;
        var tempDir = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR);
        var zipLocation = new File(tempDir + File.separator + uploadTag + "_zip_file");
        var filenameSplit = Arrays.asList(originalFilename.split("\\.(?=[^.]+$)"));
        log.debug("Filename and Extension: " + filenameSplit.toString());
        var projectForm = ProjectForm.builder().originalFile(originalFilename).build();
        var blendFile = new BlendFile();

        try {
            if (projectFile.isEmpty()) {
                throw new IOException(filename + " is empty!");
            }
            var stream = projectFile.getInputStream();
            if (Objects.requireNonNull(projectFile.getContentType()).contains("zip") ||
                    filenameSplit.get(1).contains("zip")) {
                zipLocation.mkdir();
                var storeUpload = new File(zipLocation + File.separator + filename);
                log.debug("Upload will be stored here: " + storeUpload);
                Files.copy(stream, Paths.get(storeUpload.toString()), StandardCopyOption.REPLACE_EXISTING);
                //projectFile.transferTo(storeUpload);
                var filenameWithoutExt = FilenameUtils.removeExtension(
                        originalFilename);
                projectForm.setProjectFileLocation(zipLocation + File.separator + filename);
                if (FileUtils.extractArchive(filename, zipLocation.toString())) {
                    var files = zipLocation.listFiles();
                    for (File file : files != null ? files : new File[0]) {
                        if (file.toString().contains(filenameWithoutExt + ".blend")) {
                            blendFile = BlenderUtils.parseBlendFile(file.toString(),
                                    ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR),
                                    ConfigUtils.getProperty(ConfigKeys.PYTHON_DIR));
                            if (blendFile == null) {
                                throw new IOException("Unable to read blend file");
                            }
                        }
                    }
                } else {
                    throw new IOException(originalFilename + " is not a valid archive.");
                }

                throw new IOException(originalFilename + " does not contain a blend file!");


            } else {
                if (!filenameSplit.get(1).contains("blend")) {
                    throw new IOException("This is not a valid blend file: " + originalFilename);
                }
                var storeUpload = new File(tempDir + File.separator + filename);
                log.debug("Upload will be stored here: " + storeUpload);
                Files.copy(stream, Paths.get(storeUpload.toString()), StandardCopyOption.REPLACE_EXISTING);
                //projectFile.transferTo(storeUpload);
                projectForm.setProjectFileLocation(tempDir + File.separator + filename);
                blendFile = BlenderUtils.parseBlendFile(projectForm.getProjectFileLocation(),
                        ConfigUtils.getProperty(ConfigKeys.SCRIPTS_DIR),
                        ConfigUtils.getProperty(ConfigKeys.PYTHON_DIR));
                if (blendFile == null) {
                    throw new IOException("Unable to read blend file");
                }
            }
        } catch (IOException | UnsupportedOperationException e) {
            log.error("Error saving upload: " + e.getMessage());
            log.debug(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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
                .build();

        projectForm.setProjectSettings(projectSettings);
        projectForm.setProjectType(ProjectType.STILL_IMAGE);
        projectForm.setProjectID(UUID.randomUUID().toString());
        projectForm.setProjectName("");

        return new ResponseEntity<>(projectForm, HttpStatus.OK);
    }

}
