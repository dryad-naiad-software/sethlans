/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.blender.PartCoordinates;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvinplugins.MarvinPluginCollection;
import org.apache.commons.io.FileUtils;
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
    private BlenderQueueService blenderQueueService;
    private List<String> directoriesToDelete = new ArrayList<>();


    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectServiceImpl.class);

    @Override
    @Async
    public void startProject(BlenderProject blenderProject) {
        configureFrameList(blenderProject);
        blenderQueueService.populateProjectQueue(blenderProject);
    }

    @Async
    public void cleanupProjectDir() {
        try {
            Thread.sleep(15000);
            for (String directory : directoriesToDelete) {
                FileUtils.deleteDirectory(new File(directory));

            }

        } catch (InterruptedException e) {
            LOG.debug("Cleanup interrupted");
        } catch (IOException e) {
            LOG.debug("Directory not present or root dir value null");
        }
    }

    @Override
    public void restartProject(BlenderProject blenderProject) {
        // TODO queue needs unpause and start method.
    }

    @Override
    public void pauseProject(BlenderProject blenderProject) {
        blenderQueueService.pauseRenderQueueforProject(blenderProject);
        blenderProject.setProjectStatus(ProjectStatus.Paused);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
    }

    @Override
    public void stopProject(BlenderProject blenderProject) {
        blenderQueueService.pauseRenderQueueforProject(blenderProject);
        blenderQueueService.deleteRenderQueueforProject(blenderProject);
        blenderProject.setProjectStatus(ProjectStatus.Added);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);
    }

    @Override
    public boolean deleteProject(Long id) {
        directoriesToDelete.add(blenderProjectDatabaseService.getById(id).getProjectRootDir());
        blenderProjectDatabaseService.delete(id);
        cleanupProjectDir();
        return true;
    }

    @Override
    public boolean deleteProject(String username, Long id) {
        directoriesToDelete.add(blenderProjectDatabaseService.getById(id).getProjectRootDir());
        blenderProjectDatabaseService.deleteWithVerification(username, id);
        cleanupProjectDir();
        return true;
    }

    @Override
    public boolean combineParts(BlenderProject blenderProject, int frameNumber) {
        List<String> partCleanup = new ArrayList<>();
        List<MarvinImage> images = new ArrayList<>();

        String frameFilename = null;
        String storedDir = null;
        String fileExtension = null;
        String plainFilename = null;
        for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
            if (frameNumber == blenderFramePart.getFrameNumber()) {
                images.add(MarvinImageIO.loadImage(blenderFramePart.getStoredDir() + blenderFramePart.getPartFilename() + "." + blenderFramePart.getFileExtension()));
                partCleanup.add(blenderFramePart.getStoredDir() + blenderFramePart.getPartFilename() + "." + blenderFramePart.getFileExtension());
                frameFilename = blenderFramePart.getStoredDir() + blenderFramePart.getFrameFileName() + "." + blenderFramePart.getFileExtension();
                storedDir = blenderFramePart.getStoredDir();
                fileExtension = blenderFramePart.getFileExtension();
                plainFilename = blenderFramePart.getFrameFileName();
            }
        }

        MarvinImage output = images.get(0).clone();
        MarvinImageIO.saveImage(output, frameFilename);
        blenderProject.getFrameFileNames().add(frameFilename);
        blenderProject.setCurrentFrameThumbnail(createThumbnail(frameFilename, storedDir, plainFilename, fileExtension));

        deleteParts(partCleanup);
        return true;
    }

    private void deleteParts(List<String> frameParts) {
        for (String framePart : frameParts) {
            File part = new File(framePart);
            part.delete();
        }

    }


    private void configureFrameList(BlenderProject blenderProject) {
        List<BlenderFramePart> blenderFramePartList = new ArrayList<>();
        List<String> frameFileNames = new ArrayList<>();
        List<PartCoordinates> partCoordinatesList = configurePartCoordinates(blenderProject.getPartsPerFrame());
        for (int i = 0; i < blenderProject.getTotalNumOfFrames(); i++) {
            frameFileNames.add(blenderProject.getProject_uuid() + "-" + (i + 1));
            for (int j = 0; j < blenderProject.getPartsPerFrame(); j++) {
                BlenderFramePart blenderFramePart = new BlenderFramePart();
                blenderFramePart.setFrameFileName(frameFileNames.get(i));
                blenderFramePart.setFrameNumber(i + 1);
                blenderFramePart.setPartNumber(j + 1);
                blenderFramePart.setPartFilename(blenderFramePart.getFrameFileName() + "-part" + (j + 1));
                blenderFramePart.setPartPositionMaxY(partCoordinatesList.get(j).getMax_y());
                blenderFramePart.setPartPositionMinY(partCoordinatesList.get(j).getMin_y());
                blenderFramePart.setFileExtension(blenderProject.getRenderOutputFormat().name().toLowerCase());
                blenderFramePartList.add(blenderFramePart);

            }
        }
        blenderProject.setFramePartList(blenderFramePartList);
        LOG.debug("Project Frames configured \n" + blenderFramePartList);
        blenderProjectDatabaseService.saveOrUpdate(blenderProject);

    }

    private List<PartCoordinates> configurePartCoordinates(int partsPerFrame) {
        List<PartCoordinates> partCoordinatesList = new ArrayList<>();
        Integer parts = partsPerFrame;
        Double upperLimit = 1.0;
        Double difference = upperLimit / parts;
        LOG.debug("Slice Interval " + difference);
        Double startingPoint = upperLimit;
        Double endingPoint;
        for (int i = 0; i < parts; i++) {
            LOG.debug("Starting Point " + startingPoint);
            endingPoint = startingPoint - difference;
            if (i == parts - 1) {
                endingPoint = 0.0;
            }
            PartCoordinates partCoordinates = new PartCoordinates();
            partCoordinates.setMax_y(startingPoint);
            partCoordinates.setMin_y(endingPoint);
            partCoordinatesList.add(partCoordinates);
            startingPoint = endingPoint;
            LOG.debug("Ending Point " + endingPoint);

        }
        LOG.debug("Part Coordinate List generated \n" + partCoordinatesList);

        return partCoordinatesList;
    }

    private String createThumbnail(String frameImage, String directory, String frameFilename, String fileExtension) {
        if (fileExtension.toLowerCase().contains("png")) {
            MarvinImage image = MarvinImageIO.loadImage(frameImage);
            MarvinImage thumbnail = new MarvinImage();
            MarvinPluginCollection.scale(image, thumbnail, 128, 101);
            MarvinImageIO.saveImage(thumbnail, directory + frameFilename + "-thumbnail" + "." + fileExtension);
        }
        // TODO MarvinImageIO doesn't support openEXR so that will need to be processed by another library

        return directory + frameFilename + "-thumbnail" + "." + fileExtension;
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

    @Autowired
    public void setBlenderQueueService(BlenderQueueService blenderQueueService) {
        this.blenderQueueService = blenderQueueService;
    }

}
