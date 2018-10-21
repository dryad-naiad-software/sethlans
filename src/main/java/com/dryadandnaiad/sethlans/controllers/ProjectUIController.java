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
import com.dryadandnaiad.sethlans.domains.info.ProjectInfo;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansQueryUtils.convertBlenderProjectToProjectInfo;
import static com.dryadandnaiad.sethlans.utils.SethlansQueryUtils.convertBlenderProjectsToProjectInfo;

/**
 * Created Mario Estrella on 3/27/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@RestController
@Profile({"SERVER", "DUAL"})
public class ProjectUIController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectUIController.class);
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

    @GetMapping(value = "/api/project_ui/num_of_projects")
    public Long numberOfProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return blenderProjectDatabaseService.tableSize();
        } else {
            return blenderProjectDatabaseService.listSizeByUser(auth.getName());
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
            return convertBlenderProjectsToProjectInfo(blenderProjectDatabaseService.listWithoutFramePart());
        } else {
            return convertBlenderProjectsToProjectInfo(blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(auth.getName()));
        }

    }

    @GetMapping(value = "/api/project_ui/project_name/{id}")
    public String getProjectName(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject != null) {
            return blenderProject.getProjectName();
        } else {
            return null;
        }
    }


    @GetMapping(value = "/api/project_ui/project_list_in_progress")
    public List<ProjectInfo> getUnFinishedProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            List<BlenderProject> blenderProjectList = blenderProjectDatabaseService.listWithoutFramePart();
            List<BlenderProject> unfinishedProjects = new ArrayList<>();
            for (BlenderProject blenderProject : blenderProjectList) {
                if (!blenderProject.getProjectStatus().equals(ProjectStatus.Finished)) {
                    unfinishedProjects.add(blenderProject);
                }
            }
            return convertBlenderProjectsToProjectInfo(unfinishedProjects);
        } else {
            List<BlenderProject> blenderProjectList = blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(auth.getName());
            List<BlenderProject> unfinishedProjects = new ArrayList<>();
            for (BlenderProject blenderProject : blenderProjectList) {
                if (!blenderProject.getProjectStatus().equals(ProjectStatus.Finished)) {
                    unfinishedProjects.add(blenderProject);
                }
            }
            return convertBlenderProjectsToProjectInfo(unfinishedProjects);
        }
    }

    @GetMapping("/api/project_ui/thumbnail_status/{id}")
    public boolean thumbnailPresent(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject == null) {
            return false;
        } else {
            return blenderProject.getCurrentFrameThumbnail() != null;
        }

    }

    @GetMapping("/api/project_ui/modal_image/{id}")
    public ResponseEntity<byte[]> getProjectImage(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject == null) {
            return null;
        }
        if (blenderProject.getCurrentFrameThumbnail().isEmpty()) {
            return null;
        }
        try {
            File image = new File(blenderProject.getFrameFileNames().get(0));
            InputStream in = new BufferedInputStream(new FileInputStream(image));
            byte[] imageToSend = IOUtils.toByteArray(in);
            in.close();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageToSend);

        } catch (IOException e) {
            LOG.error("No Image file found");
            return null;
        }
    }

    @GetMapping("/api/project_ui/thumbnail/{id}")
    public ResponseEntity<byte[]> getThumbnailImage(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject == null) {
            return null;
        }
        if (blenderProject.getCurrentFrameThumbnail().isEmpty()) {
            return null;
        }
        try {
            File image = new File(blenderProject.getCurrentFrameThumbnail());
            InputStream in = new BufferedInputStream(new FileInputStream(image));
            byte[] imageToSend = IOUtils.toByteArray(in);
            in.close();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageToSend);

        } catch (IOException e) {
            LOG.error("No Image file found");
            return null;
        }
    }


    @GetMapping(value = "/api/project_ui/render_time/{id}")
    public String renderTime(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getTotalRenderTime();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getTotalRenderTime();
            }
        }
        return null;
    }

    @GetMapping(value = "/api/project_ui/project_duration/{id}")
    public String projectDuration(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getProjectTime();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getProjectTime();
            }
        }
        return null;
    }

    @GetMapping(value = "/api/project_ui/completed_frames/{id}")
    public int totalNumberOfFrames(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getCompletedFrames();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return convertBlenderProjectToProjectInfo(blenderProject).getCompletedFrames();
            }
        }
        return 0;
    }

    @GetMapping(value = "/api/project_ui/total_queue/{id}")
    public int totalQueue(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return blenderProject.getTotalQueueSize();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return blenderProject.getTotalQueueSize();
            }
        }
        return 0;
    }

    @GetMapping(value = "/api/project_ui/remaining_queue/{id}")
    public int remainingQueue(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return blenderProject.getRemainingQueueSize();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return blenderProject.getRemainingQueueSize();
            }
        }
        return 0;
    }

    @GetMapping(value = "/api/project_ui/progress/{id}")
    public int currentProgress(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return blenderProject.getCurrentPercentage();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return blenderProject.getCurrentPercentage();
            }
        }
        return 0;

    }

    @GetMapping(value = "/api/project_ui/status/{id}")
    public ProjectStatus currentStatus(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return blenderProject.getProjectStatus();
            }
        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return blenderProject.getProjectStatus();
            }
        }
        return null;
    }

    @GetMapping(value = "/api/project_ui/project_details/{id}")
    public ProjectInfo getProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getByIdWithoutFrameParts(id));
        } else {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id));
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
}
