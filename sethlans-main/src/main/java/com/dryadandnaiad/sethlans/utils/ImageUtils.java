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
        var images = new ArrayList<BufferedImage>();
        var numberOfParts = project.getProjectSettings().getPartsPerFrame();
        var imageFormat = project.getProjectSettings().getImageSettings().getImageOutputFormat();
        var filenameBase = frame.getFrameFileName();
        var partDirectory = frame.getStoredDir() + File.separator + "parts";
        try {

            for (int i = 0; i < numberOfParts; i++) {
                images.add(ImageIO.read(new File(partDirectory + File.separator +
                        filenameBase + "-" + i + "." + imageFormat.name().toLowerCase())));
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

            ImageIO.write(concatImage, imageFormat.name().toUpperCase(),
                    new File(frame.getFrameFileName() + "." + imageFormat.name().toLowerCase()));

            return true;


        } catch (IOException e) {
            log.error("Unable to read image.");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }


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
