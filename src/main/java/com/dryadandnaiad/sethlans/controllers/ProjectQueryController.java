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

import com.dryadandnaiad.sethlans.converters.ProjectToProjectView;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectView;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.ImageUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class ProjectQueryController {
    private final ProjectRepository projectRepository;
    private final NodeRepository nodeRepository;
    private final ProjectToProjectView projectToProjectView;
    private final UserRepository userRepository;


    public ProjectQueryController(ProjectRepository projectRepository, NodeRepository nodeRepository,
                                  ProjectToProjectView projectToProjectView, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.nodeRepository = nodeRepository;
        this.projectToProjectView = projectToProjectView;
        this.userRepository = userRepository;
    }

    @GetMapping("/nodes_ready")
    public boolean nodesReady() {
        return nodeRepository.existsNodeByActiveIsTrue();
    }

    @GetMapping("/project_list")
    public List<ProjectView> getProjects() {
        var projectsToSend = new ArrayList<ProjectView>();
        List<Project> projects;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            projects = projectRepository.findAll();

        } else {
            if (userRepository.findUserByUsername(auth.getName()).isPresent()) {
                var user = userRepository.findUserByUsername(auth.getName()).get();
                projects = projectRepository.getProjectsByUser(user);
            } else {
                return new ArrayList<>();
            }

        }
        for (Project project : projects) {
            projectsToSend.add(projectToProjectView.convert(project));
        }
        return projectsToSend;
    }

    @GetMapping("/{project_id}")
    public ResponseEntity<ProjectView> getProject(@PathVariable("project_id") String projectID) {
        Optional<Project> project;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = projectRepository.getProjectByProjectID(projectID);
        } else {
            var user = userRepository.findUserByUsername(auth.getName()).orElse(null);
            project = projectRepository.getProjectByUserAndProjectID(user, projectID);
        }

        return project.map(value -> new ResponseEntity<>(projectToProjectView.convert(value), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{project_id}/download_video")
    public ResponseEntity<InputStreamResource> downloadVideo(@PathVariable("project_id") String projectID) {
        Project project;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = projectRepository.getProjectByProjectID(projectID).get();
        } else {
            var user = userRepository.findUserByUsername(auth.getName()).orElse(null);
            project = projectRepository.getProjectByUserAndProjectID(user, projectID).get();
        }
        try {
            var movieFile = new File(project
                    .getProjectSettings()
                    .getVideoSettings().getVideoFileLocation());
            var resource =
                    new InputStreamResource(new FileInputStream(movieFile));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" +
                            project.getProjectName().replaceAll(" ", "_")
                            + "." + project.getProjectSettings().getVideoSettings()
                            .getVideoOutputFormat().name().toLowerCase())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(movieFile.length())
                    .body(resource);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{project_id}/download_images")
    public ResponseEntity<InputStreamResource> getImages(@PathVariable("project_id") String projectID) {
        Project project;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = projectRepository.getProjectByProjectID(projectID).get();
        } else {
            var user = userRepository.findUserByUsername(auth.getName()).orElse(null);
            project = projectRepository.getProjectByUserAndProjectID(user, projectID).get();
        }
        try {
            var zipFile = new File(project
                    .getProjectSettings()
                    .getImageSettings()
                    .getImageZipFileLocation());
            var resource =
                    new InputStreamResource(new FileInputStream(zipFile));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="
                            + project.getProjectName() + ".zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipFile.length())
                    .body(resource);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{project_id}/frames/")
    public List<Frame> getProjectFrames(@PathVariable("project_id") String projectID) {
        Project project;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            project = projectRepository.getProjectByProjectID(projectID).get();
        } else {
            var user = userRepository.findUserByUsername(auth.getName()).orElse(null);
            project = projectRepository.getProjectByUserAndProjectID(user, projectID).get();
        }

        return ImageUtils.getFrameList(project);

    }
}
