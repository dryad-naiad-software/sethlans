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
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
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

    private static final Logger LOG = LoggerFactory.getLogger(BlenderProjectServiceImpl.class);

    @Override
    public void startProject(BlenderProject blenderProject) {
        configureFrameList(blenderProject);
        blenderQueueService.populateRenderQueue(blenderProject);
    }

    @Override
    public void restartProject(BlenderProject blenderProject) {

    }

    @Override
    public void pauseProject(BlenderProject blenderProject) {
        blenderQueueService.pauseRenderQueueforProject(blenderProject);
    }

    @Override
    public void stopProject(BlenderProject blenderProject) {
        blenderQueueService.pauseRenderQueueforProject(blenderProject);
        blenderQueueService.deleteRenderQueueforProject(blenderProject);
        try {
            FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        blenderProjectDatabaseService.delete(blenderProject);
    }

    @Override
    public boolean combineParts(BlenderProject blenderProject, int frameNumber) {
        String error;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        CommandLine convert;
        if (!SystemUtils.IS_OS_WINDOWS) {
            convert = new CommandLine("convert");

        } else {
            convert = new CommandLine("magick");
            convert.addArgument("convert");

        }
        String frameFilename = null;
        String storedDir = null;
        String fileExtension = null;
        String plainFilename = null;
        for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
            if (frameNumber == blenderFramePart.getFrameNumber()) {
                convert.addArgument(blenderFramePart.getStoredDir() + blenderFramePart.getPartFilename() + "." + blenderFramePart.getFileExtension());
                frameFilename = blenderFramePart.getStoredDir() + blenderFramePart.getFrameFileName() + "." + blenderFramePart.getFileExtension();
                storedDir = blenderFramePart.getStoredDir();
                fileExtension = blenderFramePart.getFileExtension();
                plainFilename = blenderFramePart.getFrameFileName();
            }
        }
        convert.addArgument("-append");
        convert.addArgument(frameFilename);
        blenderProject.getFrameFileNames().add(frameFilename);
        LOG.debug(convert.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        try {
            executor.execute(convert, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output;

            while ((output = in.readLine()) != null) {
                LOG.debug(output);
            }

            error = errorStream.toString();

            LOG.debug(error);
            blenderProject.setCurrentFrameThumbnail(createThumbnail(frameFilename, storedDir, plainFilename, fileExtension));
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
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
        String error;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        CommandLine convert;
        if (!SystemUtils.IS_OS_WINDOWS) {
            convert = new CommandLine("convert");

        } else {
            convert = new CommandLine("magick");
            convert.addArgument("convert");

        }
        convert.addArgument("-resize");
        convert.addArgument("77x60!");
        convert.addArgument(frameImage);
        convert.addArgument(directory + frameFilename + "-thumbnail" + "." + fileExtension);
        LOG.debug(convert.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        try {
            executor.execute(convert, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output;

            while ((output = in.readLine()) != null) {
                LOG.debug(output);
            }

            error = errorStream.toString();

            LOG.debug(error);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

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
