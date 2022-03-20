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

import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import com.dryadandnaiad.sethlans.enums.OS;
import com.dryadandnaiad.sethlans.enums.ProjectState;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.repositories.ProjectRepository;
import com.dryadandnaiad.sethlans.services.ServerQueueService;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;

/**
 * File created by Mario Estrella on 6/17/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@RestController
@Profile({"SERVER", "DUAL"})
@RequestMapping("/api/v1/server_queue")
public class ServerQueueController {
    private final BlenderArchiveRepository blenderArchiveRepository;
    private final NodeRepository nodeRepository;
    private final ServerQueueService serverQueueService;
    private final ProjectRepository projectRepository;

    public ServerQueueController(BlenderArchiveRepository blenderArchiveRepository, NodeRepository nodeRepository, ServerQueueService serverQueueService, ProjectRepository projectRepository) {
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.nodeRepository = nodeRepository;
        this.serverQueueService = serverQueueService;
        this.projectRepository = projectRepository;
    }


    @GetMapping(value = "/retrieve_project_file")
    public @ResponseBody
    byte[] getProjectFile(@RequestParam("system-id") String systemID,
                          @RequestParam("project-id") String projectID) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            var project = projectRepository.getProjectByProjectID(projectID).get();
            try {
                String fileToSend;
                if (project.getProjectSettings().getZipFilename() != null) {
                    fileToSend = project.getProjectRootDir() + File.separator
                            + project.getProjectSettings().getZipFilename();
                } else {
                    fileToSend = project.getProjectRootDir() + File.separator
                            + project.getProjectSettings().getBlendFilename();

                }
                var inputStream = new BufferedInputStream(new
                        FileInputStream(fileToSend));
                return IOUtils.toByteArray(inputStream);
            } catch (IOException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            }

        }
        return null;
    }

    @PostMapping(value = "/receive_task")
    public ResponseEntity<Void> receiveRenderTask(@RequestBody RenderTask renderTask) {
        if (renderTask.getServerInfo().getSystemID().equals(PropertiesUtils.getSystemID())) {
            serverQueueService.addRenderTasksToCompletedQueue(renderTask);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/retrieve_task")
    public RenderTask retrieveRenderTask(@RequestParam("system-id") String systemID) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            var node = nodeRepository.findNodeBySystemIDEquals(systemID).get();
            var renderTask = serverQueueService.retrieveRenderTaskFromPendingQueue(node.getNodeType());
            if (renderTask != null) {
                var project = projectRepository.getProjectByProjectID(renderTask.getProjectID()).get();
                renderTask.setTaskBlendFileMD5Sum(project.getProjectSettings().getBlendFilenameMD5Sum());
                renderTask.setTaskBlendFile(project.getProjectSettings().getBlendFilename());
                if (project.getProjectSettings().getZipFilename() != null) {
                    renderTask.setZipFileProject(true);
                    renderTask.setZipFile(project.getProjectSettings().getZipFilename());
                    renderTask.setZipFileMD5Sum(project.getProjectSettings().getZipFilenameMD5Sum());
                }
                if (project.getProjectStatus().getProjectState().equals(ProjectState.PENDING)) {
                    project.getProjectStatus().setProjectState(ProjectState.STARTED);
                    projectRepository.save(project);
                }
            }
            return renderTask;
        }
        return null;
    }

    @GetMapping(value = "/latest_blender_archive")
    public BlenderArchive latestBlenderArchive(@RequestParam("system-id") String systemID,
                                               @RequestParam("os") String os) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            return getLatestBlenderArchive(os);
        }
        return null;
    }

    @GetMapping(value = "/get_blender_archive")
    public BlenderArchive getBlenderArchive(@RequestParam("system-id") String systemID,
                                            @RequestParam("os") String os,
                                            @RequestParam("version") String version) {
        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            log.debug(blenderArchiveRepository.findAll().toString());
            if (blenderArchiveRepository.findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals
                    (version, OS.valueOf(os)).isPresent()) {
                return blenderArchiveRepository.
                        findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals(version, OS.valueOf(os)).get();
            }
        }
        return null;

    }


    @GetMapping(value = "/get_blender_executable")
    public @ResponseBody
    byte[] getBlenderExecutable(@RequestParam("system-id") String systemID,
                                @RequestParam("archive-os") OS archiveOS,
                                @RequestParam("archive-version") String archiveVersion) {


        if (nodeRepository.findNodeBySystemIDEquals(systemID).isPresent()) {
            try {
                log.debug(blenderArchiveRepository.findAll().toString());
                if (blenderArchiveRepository.findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals
                        (archiveVersion, archiveOS).isPresent()) {
                    var blenderArchive = blenderArchiveRepository.
                            findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals(archiveVersion, archiveOS).get();
                    InputStream inputStream = new BufferedInputStream(new
                            FileInputStream(blenderArchive.getBlenderFile()));
                    return IOUtils.toByteArray(inputStream);
                }

            } catch (IOException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            }
        }
        return null;
    }


    private BlenderArchive getLatestBlenderArchive(String os) {
        var blenderArchives = blenderArchiveRepository.findAllByDownloadedIsTrueAndBlenderOSEquals(OS.valueOf(os));
        var blenderVersions = new ArrayList<String>();
        for (BlenderArchive blenderArchive : blenderArchives) {
            blenderVersions.add(blenderArchive.getBlenderVersion());
            blenderVersions.sort(new AlphaNumericComparator());
            var selectedArchive = blenderArchiveRepository.findBlenderBinaryByBlenderVersionEqualsAndBlenderOSEquals
                    (blenderVersions.get(0), OS.valueOf(os));
            return selectedArchive.orElse(null);
        }
        return null;
    }

}
