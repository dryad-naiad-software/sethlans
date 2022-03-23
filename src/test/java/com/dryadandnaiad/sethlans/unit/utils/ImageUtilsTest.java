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

package com.dryadandnaiad.sethlans.unit.utils;

import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.tools.TestFileUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import com.dryadandnaiad.sethlans.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 5/9/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class ImageUtilsTest {

    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + "testing");


    @BeforeEach
    void setUp() {
        TEST_DIRECTORY.mkdirs();
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_DIRECTORY);
    }

    @Test
    void createThumbnailPNG() {
        var imageDir = new File(TEST_DIRECTORY + File.separator + "frames");
        imageDir.mkdirs();
        var file1 = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1.png";
        TestFileUtils.copyTestArchiveToDisk(imageDir.toString(), "frame/" + file1, file1);
        var filename = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1";
        var thumbnailsDir = new File(TEST_DIRECTORY + File.separator + "thumbnails");
        thumbnailsDir.mkdirs();
        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .fileExtension("png")
                .imageDir(imageDir.toString())
                .thumbsDir(thumbnailsDir.toString())
                .build();
        Assertions.assertThat(ImageUtils.createThumbnail(frame)).isTrue();
    }

    @Test
    void createThumbnailTIFF() {
        var imageDir = new File(TEST_DIRECTORY + File.separator + "frames");
        imageDir.mkdirs();
        var file1 = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1.tiff";
        TestFileUtils.copyTestArchiveToDisk(imageDir.toString(), "frame/" + file1, file1);
        var filename = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1";
        var thumbnailsDir = new File(TEST_DIRECTORY + File.separator + "thumbnails");
        thumbnailsDir.mkdirs();
        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .fileExtension("tiff")
                .imageDir(imageDir.toString())
                .thumbsDir(thumbnailsDir.toString())
                .build();
        assertThat(ImageUtils.createThumbnail(frame)).isTrue();
    }

    @Test
    void createThumbnailHDR() {
        var imageDir = new File(TEST_DIRECTORY + File.separator + "frames");
        imageDir.mkdirs();
        var file1 = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1.hdr";
        TestFileUtils.copyTestArchiveToDisk(imageDir.toString(), "frame/" + file1, file1);
        var filename = "fba51634-4fe3-4c54-880d-71ab67016472-frame-1";
        var thumbnailsDir = new File(TEST_DIRECTORY + File.separator + "thumbnails");
        thumbnailsDir.mkdirs();

        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .fileExtension("hdr")
                .imageDir(imageDir.toString())
                .thumbsDir(thumbnailsDir.toString())
                .build();
        assertThat(ImageUtils.createThumbnail(frame)).isTrue();
    }


    @Test
    void configurePartCoordinates() {
        assertThat(ImageUtils.configurePartCoordinates(4)).hasSize(4);
    }

    @Test
    void combine4PartsPNG() {
        var file1 = "png-4-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("png")
                .partsPerFrame(4)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine9PartsPNGBarcelone() {
        var file1 = "png-9-parts-barcelone.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("png")
                .partsPerFrame(9)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine4PartsHDR() {
        var file1 = "hdr-4-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("hdr")
                .partsPerFrame(4)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();

    }

    @Test
    void combine9PartsHDR() {
        var file1 = "hdr-9-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("hdr")
                .partsPerFrame(9)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();
    }

    @Test
    void combine9PartsHDRBarcelone() {
        var file1 = "hdr-9-parts-barcelone.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("hdr")
                .partsPerFrame(9)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();
    }

    @Test
    void combine9PartsPNG() {
        var file1 = "png-9-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("png")
                .partsPerFrame(9)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine4PartsTIFF() {
        var file1 = "tiff-4-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString(), true);
        var frame = Frame.builder()
                .frameName("fba51634-4fe3-4c54-880d-71ab67016472-frame-1")
                .fileExtension("tiff")
                .partsPerFrame(4)
                .frameNumber(1)
                .imageDir(TEST_DIRECTORY + File.separator + "frames")
                .partsDir(partLocation.toString())
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.TIFF)).isTrue();
    }

}
