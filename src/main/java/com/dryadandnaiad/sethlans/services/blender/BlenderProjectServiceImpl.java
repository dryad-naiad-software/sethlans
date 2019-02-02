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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.blender.PartCoordinates;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderProjectServiceImpl implements BlenderProjectService {
    private BlenderProjectDatabaseService blenderProjectDatabaseService;
    private QueueService queueService;
    private SethlansNotificationService sethlansNotificationService;
    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectServiceImpl.class);

    @Override
    @Async
    public void startProject(BlenderProject blenderProject) {
        configureFrameList(blenderProject);
        String message = blenderProject.getProjectName() + " has been started";
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.PROJECT, message, blenderProject.getSethlansUser().getUsername());
        sethlansNotification.setMailable(true);
        sethlansNotification.setLinkPresent(true);
        sethlansNotification.setMessageLink("/projects/view/" + blenderProject.getId());
        sethlansNotification.setSubject(blenderProject.getProjectName());
    }

    @Override
    public void resumeProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        sendResumeToQueueService(blenderProject);
    }


    @Override
    public void resumeProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        sendResumeToQueueService(blenderProject);

    }

    private void sendResumeToQueueService(BlenderProject blenderProject) {
        if (!blenderProject.isAllImagesProcessed()) {
            queueService.resumeBlenderProjectQueue(blenderProject);
        }
    }

    @Override
    public void pauseProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        sendPauseToQueueService(blenderProject);

    }

    @Override
    public void pauseProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        sendPauseToQueueService(blenderProject);
    }

    private void sendPauseToQueueService(BlenderProject blenderProject) {
        if (!blenderProject.isAllImagesProcessed()) {
            queueService.pauseBlenderProjectQueue(blenderProject);
        }
    }

    @Override
    public void stopProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        queueService.stopBlenderProjectQueue(blenderProject);
        int count = blenderProject.getFrameFileNames().size();
        deleteProjectFrames(blenderProject, count);
        String message = blenderProject.getProjectName() + " has been stopped";
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.PROJECT, message, blenderProject.getSethlansUser().getUsername());
        sethlansNotification.setMailable(true);
        sethlansNotification.setLinkPresent(true);
        sethlansNotification.setMessageLink("/projects/view/" + blenderProject.getId());
        sethlansNotification.setSubject(blenderProject.getProjectName());
        sethlansNotificationService.sendNotification(sethlansNotification);
    }

    @Override
    public void stopProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        queueService.stopBlenderProjectQueue(blenderProject);
        int count = blenderProject.getFrameFileNames().size();
        deleteProjectFrames(blenderProject, count);
        String message = blenderProject.getProjectName() + " has been stopped";
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.PROJECT, message, blenderProject.getSethlansUser().getUsername());
        sethlansNotification.setMailable(true);
        sethlansNotification.setLinkPresent(true);
        sethlansNotification.setMessageLink("/projects/view/" + blenderProject.getId());
        sethlansNotificationService.sendNotification(sethlansNotification);

    }


    @Override
    public void deleteProject(Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getById(id);
        if (blenderProject.getProjectStatus().equals(ProjectStatus.Finished) || blenderProject.getProjectStatus().equals(ProjectStatus.Added)) {
            String directory = blenderProject.getProjectRootDir();
            blenderProjectDatabaseService.delete(id);
            deleteDirectory(directory);
            String message = blenderProject.getProjectName() + " deleted successfully";
            SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.PROJECT, message, blenderProject.getSethlansUser().getUsername());
            sethlansNotificationService.sendNotification(sethlansNotification);
        }

    }

    @Override
    public void deleteProject(String username, Long id) {
        BlenderProject blenderProject = blenderProjectDatabaseService.getProjectByUser(username, id);
        if (blenderProject.getProjectStatus().equals(ProjectStatus.Finished) || blenderProject.getProjectStatus().equals(ProjectStatus.Added)) {
            String directory = blenderProject.getProjectRootDir();
            blenderProjectDatabaseService.deleteWithVerification(username, id);
            deleteDirectory(directory);
            String message = blenderProject.getProjectName() + " deleted successfully";
            SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.PROJECT, message, blenderProject.getSethlansUser().getUsername());
            sethlansNotificationService.sendNotification(sethlansNotification);
        }
    }

    @Override
    public void deleteAllProjects() {
        List<BlenderProject> allProjects = blenderProjectDatabaseService.listAll();
        for (BlenderProject project : allProjects) {
            deleteProject(project.getId());
        }
    }

    @Override
    public void deleteAllUserProjects(String username) {
        List<BlenderProject> allUserProjects = blenderProjectDatabaseService.getProjectsByUserWithoutFrameParts(username);
        for (BlenderProject project : allUserProjects) {
            deleteProject(project.getId());
        }

    }


    private void deleteDirectory(String directory) {
        try {
            Thread.sleep(1000);
            FileUtils.deleteDirectory(new File(directory));
        } catch (InterruptedException | IOException e) {
            LOG.error("Error occurred deleting project " + e.getMessage());
        }
    }


    private void deleteProjectFrames(BlenderProject blenderProject, int count) {
        for (int i = 0; i <= count; i++) {
            int frame = i + 1;
            try {
                FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir() + File.separator + "frame_" + frame));
            } catch (IOException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
    }

    private void configureFrameList(BlenderProject blenderProject) {
        List<BlenderFramePart> blenderFramePartList = new ArrayList<>();
        List<String> frameFileNames = new ArrayList<>();
        String truncatedProjectName = StringUtils.left(blenderProject.getProjectName(), 10);
        String truncatedUUID = StringUtils.left(blenderProject.getProjectUUID(), 4);
        String cleanedProjectName = truncatedProjectName.replaceAll(" ", "").replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();
        List<PartCoordinates> partCoordinatesList = configurePartCoordinates(blenderProject.getPartsPerFrame());
        for (int frames = 0; frames < blenderProject.getTotalNumOfFrames(); frames++) {
            frameFileNames.add(cleanedProjectName + "-" + truncatedUUID + "-" + (frames + 1));
            for (int parts = 0; parts < partCoordinatesList.size(); parts++) {
                BlenderFramePart blenderFramePart = new BlenderFramePart();
                blenderFramePart.setFrameFileName(frameFileNames.get(frames));
                blenderFramePart.setFrameNumber(frames + 1);
                blenderFramePart.setPartNumber(parts + 1);
                blenderFramePart.setPartFilename(blenderFramePart.getFrameFileName() + "-part" + (parts + 1));
                blenderFramePart.setPartPositionMaxX(partCoordinatesList.get(parts).getMax_x());
                blenderFramePart.setPartPositionMinX(partCoordinatesList.get(parts).getMin_x());
                blenderFramePart.setPartPositionMaxY(partCoordinatesList.get(parts).getMax_y());
                blenderFramePart.setPartPositionMinY(partCoordinatesList.get(parts).getMin_y());
                blenderFramePart.setFileExtension("png");
                blenderFramePartList.add(blenderFramePart);
            }
        }
        blenderProject.setFramePartList(blenderFramePartList);
        blenderProject.setTotalQueueSize(blenderFramePartList.size());
        blenderProject.setQueueIndex(0);
        blenderProject.setCompletedFrames(0);
        blenderProject.setTotalNumberOfFrames((blenderProject.getEndFrame() - blenderProject.getStartFrame()) + 1);
        blenderProject.setRemainingQueueSize(blenderFramePartList.size());
        LOG.debug("Project Frames configured.");
        blenderProject.setProjectStatus(ProjectStatus.Pending);
        blenderProject.setUserStopped(false);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);

    }

    private List<PartCoordinates> configurePartCoordinates(int partsPerFrame) {
        double sqrtOfPart = Math.sqrt(partsPerFrame);
        int endRange = (int) sqrtOfPart;
        List<PartCoordinates> partCoordinatesList = new ArrayList<>();
        for (int r = 0; r < endRange; r++) {
            int row = r + 1;
            for (int c = 0; c < endRange; c++) {
                int col = c + 1;
                PartCoordinates partCoordinates = new PartCoordinates();
                partCoordinates.setMin_x((col - 1) / sqrtOfPart);
                partCoordinates.setMax_x(col / sqrtOfPart);
                partCoordinates.setMin_y((sqrtOfPart - row) / sqrtOfPart);
                partCoordinates.setMax_y((sqrtOfPart - row + 1) / sqrtOfPart);
                partCoordinatesList.add(partCoordinates);
            }
        }
        LOG.debug("Part Coordinate List generated " + partCoordinatesList);
        LOG.debug("Number of elements " + partCoordinatesList.size());

        return partCoordinatesList;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}
