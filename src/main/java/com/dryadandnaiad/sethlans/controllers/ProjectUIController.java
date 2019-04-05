/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.domains.database.queue.QueueHistoryItem;
import com.dryadandnaiad.sethlans.domains.info.ProjectInfo;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.database.QueueHistoryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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
@RequestMapping("/api/project_ui")
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
    private QueueHistoryDatabaseService queueHistoryDatabaseService;

    @GetMapping(value = "/num_of_projects")
    public Long numberOfProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return blenderProjectDatabaseService.tableSize();
        } else {
            return blenderProjectDatabaseService.listSizeByUser(auth.getName());
        }
    }

    @GetMapping(value = "/nodes_ready")
    public boolean nodesReady() {
        return sethlansNodeDatabaseService.activeNodes();
    }

    @GetMapping(value = "/project_list")
    public List<ProjectInfo> getProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            List<BlenderProject> blenderProjects = blenderProjectDatabaseService.listWithoutFramePart();
            blenderProjects.sort(Collections.reverseOrder());
            return convertBlenderProjectsToProjectInfo(Lists.reverse(blenderProjects));
        } else {
            List<BlenderProject> blenderProjects = blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(auth.getName());
            return convertBlenderProjectsToProjectInfo(Lists.reverse(blenderProjects));
        }

    }

    @GetMapping(value = "/last_five_projects")
    public List<ProjectInfo> getLastFiveProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            List<BlenderProject> projects = blenderProjectDatabaseService.listWithoutFramePart();
            List<BlenderProject> last5projects = projects.subList(Math.max(projects.size() - 5, 0), projects.size());
            return convertBlenderProjectsToProjectInfo(Lists.reverse(last5projects));
        } else {
            List<BlenderProject> projects = blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(auth.getName());
            List<BlenderProject> last5projects = projects.subList(Math.max(projects.size() - 5, 0), projects.size());
            return convertBlenderProjectsToProjectInfo(Lists.reverse(last5projects));
        }
    }

    @GetMapping(value = "/project_name/{id}")
    public String getProjectName(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject != null) {
            return blenderProject.getProjectName();
        } else {
            return null;
        }
    }



    @GetMapping(value = "/project_list_in_progress")
    public List<ProjectInfo> getUnFinishedProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<BlenderProject> blenderProjectList;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProjectList = blenderProjectDatabaseService.listWithoutFramePart();
        } else {
            blenderProjectList = blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(auth.getName());
        }
        if (blenderProjectList == null) {
            return null;
        }
        List<BlenderProject> unfinishedProjects = new ArrayList<>();
            for (BlenderProject blenderProject : blenderProjectList) {
                if (!blenderProject.getProjectStatus().equals(ProjectStatus.Finished)) {
                    unfinishedProjects.add(blenderProject);
                }
            }
            return convertBlenderProjectsToProjectInfo(unfinishedProjects);
    }

    @GetMapping("/thumbnail_status/{id}")
    public boolean thumbnailPresent(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (blenderProject == null) {
            return false;
        } else {
            return blenderProject.getCurrentFrameThumbnail() != null;
        }

    }

    private boolean checkProjectState(BlenderProject blenderProject) {
        if (blenderProject == null) {
            return false;
        } else return blenderProject.getCurrentFrameThumbnail() != null;
    }

    private ResponseEntity<byte[]> sendImage(File image) {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(image));
            byte[] imageToSend = IOUtils.toByteArray(in);
            in.close();
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageToSend);

        } catch (IOException e) {
            LOG.error("No Image file found");
            return null;
        }
    }

    @GetMapping("/images/{id}/frame")
    public ResponseEntity<byte[]> getFrameImage(@PathVariable Long id, @RequestParam int number) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (checkProjectState(blenderProject)) {
            for (String fileName :
                    blenderProject.getFrameFileNames()) {
                if (fileName.contains(File.separator + "frame_" + number + File.separator)) {
                    File image = new File(fileName);
                    return sendImage(image);
                }

            }

        }
        return null;
    }

    @GetMapping("/images/{id}/thumbnail")
    public ResponseEntity<byte[]> getFrameThumbnail(@PathVariable Long id, @RequestParam int number) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (checkProjectState(blenderProject)) {
            for (String fileName :
                    blenderProject.getThumbnailFileNames()) {
                if (fileName.contains(File.separator + "frame_" + number + File.separator)) {
                    File image = new File(fileName);
                    return sendImage(image);
                }

            }

        }
        return null;
    }


    @GetMapping("/completed_frame_ids/{id}")
    public List<Integer> getCompleteFrameIds(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (checkProjectState(blenderProject)) {
            String firstTag = File.separator + "frame_";
            String lastTag = File.separator;
            List<Integer> completedIds = new ArrayList<>();
            for (String frameFileName : blenderProject.getFrameFileNames()) {
                completedIds.add(Integer.parseInt(StringUtils.substringBetween(frameFileName, firstTag, lastTag)));
            }
            return completedIds;
        }
        return null;
    }


    @GetMapping("/still_image/{id}")
    public ResponseEntity<byte[]> getProjectImage(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (checkProjectState(blenderProject)) {
            File image = new File(blenderProject.getFrameFileNames().get(0));
            return sendImage(image);
        }
        return null;
    }

    @GetMapping("/current_thumbnail/{id}")
    public ResponseEntity<byte[]> getThumbnailImage(@PathVariable Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
        if (checkProjectState(blenderProject)) {
            File image = new File(blenderProject.getCurrentFrameThumbnail());
            return sendImage(image);
        }
        return null;
    }


    @GetMapping(value = "/render_time/{id}")
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

    @GetMapping(value = "/project_duration/{id}")
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

    @GetMapping(value = "/completed_frames/{id}")
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

    @GetMapping(value = "/total_queue/{id}")
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

    @GetMapping(value = "/remaining_queue/{id}")
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

    @GetMapping(value = "/progress/{id}")
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

    @GetMapping(value = "/status/{id}")
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

    @GetMapping(value = "/project_details/{id}")
    public ProjectInfo getProject(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getByIdWithoutFrameParts(id));
        } else {
            return convertBlenderProjectToProjectInfo(blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id));
        }
    }

    @GetMapping(value = "/project_queue/{id}")
    public List<QueueHistoryItem> getProjectQueueHistory(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        BlenderProject blenderProject;
        if (auth.getAuthorities().toString().contains("ADMINISTRATOR")) {
            blenderProject = blenderProjectDatabaseService.getByIdWithoutFrameParts(id);
            if (blenderProject != null) {
                return queueHistoryDatabaseService.getProjectQueueHistory(blenderProject.getProjectUUID());
            }

        } else {
            blenderProject = blenderProjectDatabaseService.getProjectByUserWithoutFrameParts(auth.getName(), id);
            if (blenderProject != null) {
                return queueHistoryDatabaseService.getProjectQueueHistory(blenderProject.getProjectUUID());
            }
        }
        return null;
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
    public void setQueueHistoryDatabaseService(QueueHistoryDatabaseService queueHistoryDatabaseService) {
        this.queueHistoryDatabaseService = queueHistoryDatabaseService;
    }
}
