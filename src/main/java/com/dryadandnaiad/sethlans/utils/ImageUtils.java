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

import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.tasks.TaskFrameInfo;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.util.FileSystemUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.io.FileUtils.copyFile;


/**
 * File created by Mario Estrella on 5/9/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class ImageUtils {

    public static boolean combineParts(Frame frame, ImageOutputFormat imageOutputFormat) {
        try (Stream<Path> files = Files.list(Paths.get(frame.getPartsDir()))) {
            var count = files.count();
            if (count != frame.getPartsPerFrame()) {
                return false;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        log.info("Combining parts for " + frame.getFrameName());

        if (imageOutputFormat.equals(ImageOutputFormat.HDR)) {
            return combineHDR(frame);
        }
        var images = new ArrayList<BufferedImage>();
        var numberOfParts = frame.getPartsPerFrame();
        var filenameBase = frame.getFrameName();
        var partDirectory = frame.getPartsDir();

        try {

            for (int i = 0; i < numberOfParts; i++) {
                var filename = new File(partDirectory + File.separator +
                        filenameBase + "-part-" + (i + 1) + "." + frame.getFileExtension());
                log.debug("Processing part: " + filename);
                images.add(ImageIO.read(filename));
            }
            if (images.size() == 0) {
                log.error("Unable to process images loaded.");
                return false;
            }

            int squareRootOfParts = (int) Math.sqrt(numberOfParts);

            BufferedImage concatImage = new BufferedImage(
                    images.get(0).getWidth() * squareRootOfParts, images.get(0).getHeight() * squareRootOfParts,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics g = concatImage.getGraphics();

            int x = 0;
            int y = 0;
            for (BufferedImage image : images) {
                g.drawImage(image, x, y, null);
                x += image.getWidth();
                if (x >= concatImage.getWidth()) {
                    x = 0;
                    y += image.getHeight();
                }
            }

            var frameFilename = new File(frame.getImageDir() + File.separator +
                    frame.getFrameName() + "." + imageOutputFormat.name().toLowerCase());

            if (!ImageIO.write(concatImage, imageOutputFormat.name().toUpperCase(), frameFilename
            )) {
                log.error("Unable to combine images.");
                return false;
            }

            log.info("Completed processing of " + frameFilename);


            // Part Cleanup
            for (int i = 0; i < numberOfParts; i++) {
                var filename = new File(partDirectory + File.separator +
                        filenameBase + "-part-" + (i + 1) + "." + frame.getFileExtension());
                log.debug("Deleting part " + filename);
                filename.delete();
            }

            return true;


        } catch (IOException e) {
            log.error("Unable to read image.");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    public static boolean combineHDR(Frame frame) {
        nu.pattern.OpenCV.loadLocally();
        var numberOfParts = frame.getPartsPerFrame();
        var filenameBase = frame.getFrameName();
        var partDirectory = frame.getPartsDir();
        int squareRootOfParts = (int) Math.sqrt(numberOfParts);
        var imageArrays = new ArrayList<ArrayList<Mat>>();


        try {
            // Create image arrays equal to the square root number of parts.
            for (int i = 0; i < squareRootOfParts; i++) {
                imageArrays.add(new ArrayList<>());
            }
            for (int i = 0; i < numberOfParts; i++) {
                var filename = new File(partDirectory + File.separator +
                        filenameBase + "-part-" + (i + 1) + "." + frame.getFileExtension());
                log.debug("Processing part: " + filename.toString());
                var arrayId = 0;
                var currentArray = imageArrays.get(arrayId);
                // Each array equals one row of images.  Move to the next array once the row is complete.
                while (currentArray.size() > squareRootOfParts - 1) {
                    arrayId++;
                    currentArray = imageArrays.get(arrayId);
                }
                currentArray.add(Imgcodecs.imread(filename.toString(), Imgcodecs.IMREAD_UNCHANGED));

            }
            for (ArrayList<Mat> imageArray : imageArrays) {
                if (imageArray.size() == 0) {
                    log.error("Unable to process images loaded.");
                    return false;
                }
            }

            var frameFilename = new File(frame.getImageDir() + File.separator +
                    frame.getFrameName() + "." + "hdr");

            var rowResult = new ArrayList<Mat>();

            // Combine images within rows
            for (int row = 0; row < imageArrays.size(); row++) {
                rowResult.add(row, new Mat());
                Core.hconcat(imageArrays.get(row), rowResult.get(row));
            }

            var finalResult = new Mat();

            // Combine rows
            Core.vconcat(rowResult, finalResult);
            if (!Imgcodecs.imwrite(frameFilename.toString(), finalResult)) {
                log.error("Unable to combine images.");
                return false;
            }
            log.info("Completed processing of " + frameFilename.toString());

            // Part Cleanup
            for (int i = 0; i < numberOfParts; i++) {
                var filename = new File(partDirectory + File.separator +
                        filenameBase + "-part-" + (i + 1) + "." + "hdr");
                log.debug("Deleting part " + filename);
                filename.delete();
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }

        return true;
    }


    public static boolean createThumbnail(Frame frame) {
        var originalImage = new File(frame.getImageDir()
                + File.separator + frame.getFrameName() + "." + frame.getFileExtension());
        var thumbnail = new File(frame.getThumbsDir() + File.separator
                + frame.getFrameName() + "-thumbnail" + "." + "png");
        log.info("Creating thumbnail for " + originalImage);
        try {
            Thumbnails.of(originalImage).size(300, 300).toFile(thumbnail);
            log.info("Thumbnail " + thumbnail + " successfully created.");
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }

    }

    public static List<TaskFrameInfo> configurePartCoordinates(int partsPerFrame) {
        List<TaskFrameInfo> partCoordinatesList = new ArrayList<>();
        var partsPerRow = (int) Math.sqrt(partsPerFrame);
        var slices = 1.0 / partsPerRow;

        for (int y = 0; y < partsPerRow; y++) {
            for (int x = 0; x < partsPerRow; x++) {
                TaskFrameInfo partCoordinates = new TaskFrameInfo();
                partCoordinates.setPartMinX(slices * x);
                partCoordinates.setPartMaxX(slices * (x + 1));
                partCoordinates.setPartMinY(1.0 - (slices * (y + 1)));
                partCoordinates.setPartMaxY(1.0 - (slices * y));
                partCoordinatesList.add(partCoordinates);
            }
        }

        log.debug("Part Coordinate List generated");
        return partCoordinatesList;
    }

    public static String createZipFileFromImages(Project blenderProject) {
        var frameList = getFrameList(blenderProject);

        var tempDir = new File(blenderProject.getProjectRootDir()
                + File.separator + "images-" + QueryUtils.getShortUUID());
        tempDir.mkdirs();

        var files = new ArrayList<String>();

        for (Frame frame : frameList) {
            var originalImage = new File(frame.getImageDir()
                    + File.separator + frame.getFrameName() + "." + frame.getFileExtension());
            var newImage = new File(tempDir + File.separator + "image" + "-"
                    + frame.getFrameNumber() + "." + frame.getFileExtension());
            try {
                log.debug("Copying " + frame.getFrameName() + " to " + newImage);
                copyFile(originalImage, newImage);
                files.add(newImage.toString());
            } catch (IOException e) {
                log.error("Error copying file: " + originalImage);
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }
        }

        var zipFile = FileUtils.createZipArchive(files, blenderProject.getProjectRootDir(),
                blenderProject.getProjectName());

        if (zipFile.exists()) {
            FileSystemUtils.deleteRecursively(tempDir);
            return zipFile.toString();
        }
        return null;
    }

    public static List<Frame> getFrameList(Project blenderProject) {
        List<Frame> frameList = new ArrayList<>();

        for (int i = 0; i < blenderProject.getProjectSettings().getTotalNumberOfFrames(); i++) {
            var frame = Frame.builder()
                    .frameName(blenderProject.getProjectID() + "-frame-" + (i + 1))
                    .frameNumber(i + 1)
                    .imageDir(blenderProject.getProjectRootDir() + File.separator + "images")
                    .thumbsDir(blenderProject.getProjectRootDir() + File.separator + "thumbnails")
                    .fileExtension(blenderProject
                            .getProjectSettings()
                            .getImageSettings()
                            .getImageOutputFormat()
                            .name().toLowerCase())
                    .build();
            frameList.add(frame);
        }
        return frameList;
    }
}
