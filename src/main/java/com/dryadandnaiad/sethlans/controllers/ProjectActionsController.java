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

import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.services.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final NodeRepository nodeRepository;

    public ProjectActionsController(ProjectService projectService, NodeRepository nodeRepository) {
        this.projectService = projectService;
        this.nodeRepository = nodeRepository;
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
        return projectService.projectFileUpload(projectFile);
    }

    @PostMapping("/create_project")
    public ResponseEntity<Void> createProject(@RequestBody ProjectForm projectForm) {
        if (projectService.createProject(projectForm)) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/start_project")
    public ResponseEntity<Void> startProject(@RequestParam String projectID) {
        if (nodeRepository.existsNodeByActiveIsTrue()) {
            if (projectService.startProject(projectID)) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
