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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.services.ProjectService;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
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
import java.util.Arrays;
import java.util.Objects;

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

    public ProjectActionsController(ProjectService projectService) {
        this.projectService = projectService;
    }


    @DeleteMapping("/delete_all_projects")
    @ResponseStatus(HttpStatus.OK)
    public void deleteAllProjects() {
        projectService.deleteAllProjects();
    }

    @PostMapping("/upload_new_project")
    public ResponseEntity<ProjectForm> newProjectUpload(@RequestParam("project_file") MultipartFile projectFile) {
        var originalFilename = Objects.requireNonNull(projectFile.getOriginalFilename()).toLowerCase();
        var uploadTag = QueryUtils.getShortUUID();
        log.info(originalFilename + " uploaded.");
        var filename = uploadTag + "-" + originalFilename;
        var tempDir = ConfigUtils.getProperty(ConfigKeys.TEMP_DIR);
        var zipLocation = new File(tempDir + File.separator + uploadTag + "_zip_file");
        var filenameSplit = Arrays.asList(originalFilename.split("\\.(?=[^.]+$)"));
        log.debug("Filename and Extension: " + filenameSplit.toString());
        var projectForm = new ProjectForm();

        try {
            if (Objects.requireNonNull(projectFile.getContentType()).contains("zip") || filenameSplit.get(1).contains("zip")) {
                zipLocation.mkdir();
                var storeUpload = new File(zipLocation + File.separator + filename);
                projectFile.transferTo(storeUpload);
                var filenameWithoutExt = FilenameUtils.removeExtension(
                        originalFilename);

            } else {
                var storeUpload = new File(tempDir + File.separator + uploadTag + "-" +
                        projectFile.getOriginalFilename().toLowerCase());
                projectFile.transferTo(storeUpload);
            }
        } catch (IOException e) {
            log.error("Error saving upload " + e.getMessage());
            log.debug(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


    }

}
