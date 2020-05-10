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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.frames.Part;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * File created by Mario Estrella on 5/9/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class ImageUtils {

    public static boolean combineParts(Project project, Frame frame) {
        var images = new ArrayList<MBFImage>();
        var numberOfParts = project.getProjectSettings().getPartsPerFrame();
        var imageFormat = project.getProjectSettings().getImageSettings().getImageOutputFormat();
        var filenameBase = frame.getFrameFileName();
        var partDirectory = frame.getStoredDir() + File.separator + "parts";
        try {

            for (int i = 0; i < numberOfParts; i++) {
                images.add(ImageUtilities.readMBF(new File(partDirectory + File.separator + filenameBase + "-" + i + "." + imageFormat.name().toLowerCase())));
            }
        } catch (IOException e) {
            log.error("Unable to read image.");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }

        return false;
    }

//    public static boolean combineParts(BlenderProject blenderProject, int frameNumber) {
//        List<String> partCleanup = new ArrayList<>();
//        List<BufferedImage> images = new ArrayList<>();
//        String frameFilename = null;
//        String storedDir = null;
//        String fileExtension = null;
//        String plainFilename = null;
//        LOG.debug("Combining images");
//        int errorCount = 0;
//        for (BlenderFramePart blenderFramePart : blenderProject.getFramePartList()) {
//            if (frameNumber == blenderFramePart.getFrameNumber()) {
//                try {
//                    String file = blenderFramePart.getStoredDir() + blenderFramePart.getPartFilename() + "." + blenderFramePart.getFileExtension();
//                    LOG.debug("Reading file " + file);
//                    images.add(ImageIO.read(new File(file)));
//                    partCleanup.add(blenderFramePart.getStoredDir() + blenderFramePart.getPartFilename() + "." + blenderFramePart.getFileExtension());
//                    frameFilename = blenderFramePart.getStoredDir() + blenderFramePart.getFrameFileName() + "." + blenderFramePart.getFileExtension();
//                    storedDir = blenderFramePart.getStoredDir();
//                    fileExtension = blenderFramePart.getFileExtension();
//                    plainFilename = blenderFramePart.getFrameFileName();
//                } catch (IOException e) {
//                    LOG.error("Unable to read image.");
//                    errorCount++;
//                    LOG.error(Throwables.getStackTraceAsString(e));
//                }
//            }
//        }
//        try {
//            int squareRootOfParts = (int) Math.sqrt(blenderProject.getPartsPerFrame());
//
//            BufferedImage concatImage = new BufferedImage(
//                    images.get(0).getWidth() * squareRootOfParts, images.get(0).getHeight() * squareRootOfParts,
//                    BufferedImage.TYPE_INT_ARGB);
//            Graphics g = concatImage.getGraphics();
//
//            int x = 0;
//            int y = 0;
//            for (BufferedImage image : images) {
//                g.drawImage(image, x, y, null);
//                x += image.getWidth();
//                if (x >= concatImage.getWidth()) {
//                    x = 0;
//                    y += image.getHeight();
//                }
//            }
//
//            ImageIO.write(concatImage, fileExtension.toUpperCase(), new File(frameFilename));
//            FrameFileUpdateItem newFrameUpdate = new FrameFileUpdateItem();
//            newFrameUpdate.setProjectUUID(blenderProject.getProjectUUID());
//            newFrameUpdate.setFrameFileName(frameFilename);
//            newFrameUpdate.setCurrentFrameThumbnail(createThumbnail(frameFilename, storedDir, plainFilename, fileExtension));
//            frameFileUpdateDatabaseService.saveOrUpdate(newFrameUpdate);
//
//        } catch (IOException e) {
//            LOG.error(Throwables.getStackTraceAsString(e));
//        } catch (IndexOutOfBoundsException e) {
//            LOG.error("Possible node idle collision");
//            LOG.error(Throwables.getStackTraceAsString(e));
//        }
//
//
//        if (errorCount == 0) {
//            LOG.debug("Images combined successfully, deleting parts...");
//            deleteParts(partCleanup);
//        } else {
//            LOG.debug("Some images are not complete, will reattempt to combine them.");
//        }
//        return true;
//    }

    public static String createThumbnail(String frameImage, String directory,
                                         String frameFilename, String fileExtension) {
        try {
            BufferedImage image = ImageIO.read(new File(frameImage));
            BufferedImage thumbnail = new BufferedImage(128, 101, image.getType());
            Graphics2D g = thumbnail.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, 128, 101, 0, 0, image.getWidth(),
                    image.getHeight(), null);
            g.dispose();
            ImageIO.write(thumbnail, fileExtension.toUpperCase(), new File(directory + frameFilename +
                    "-thumbnail" + "." + fileExtension));
        } catch (IOException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }

        return directory + frameFilename + "-thumbnail" + "." + fileExtension;
    }

    public static List<Part> configurePartCoordinates(int partsPerFrame) {
        double sqrtOfPart = Math.sqrt(partsPerFrame);
        int endRange = (int) sqrtOfPart;
        List<Part> partCoordinatesList = new ArrayList<>();
        for (int r = 0; r < endRange; r++) {
            int row = r + 1;
            for (int c = 0; c < endRange; c++) {
                int col = c + 1;
                Part partCoordinates = new Part();
                partCoordinates.setMinX((col - 1) / sqrtOfPart);
                partCoordinates.setMaxX(col / sqrtOfPart);
                partCoordinates.setMaxY((sqrtOfPart - row) / sqrtOfPart);
                partCoordinates.setMaxY((sqrtOfPart - row + 1) / sqrtOfPart);
                partCoordinatesList.add(partCoordinates);
            }
        }
        log.debug("Part Coordinate List generated");
        return partCoordinatesList;
    }
}
