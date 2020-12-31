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
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.testutils.TestFileUtils;
import com.dryadandnaiad.sethlans.testutils.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var file1 = "asamplep-fd4e-0001.png";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "frame/" + file1, file1);
        var filename = "asamplep-fd4e-0001";
        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .combined(true)
                .storedDir(TEST_DIRECTORY.toString())
                .fileExtension("png")
                .build();
        assertThat(ImageUtils.createThumbnail(frame)).isTrue();
    }

    @Test
    void createThumbnailTIFF() {
        var file1 = "asamplep-1127-0001.tif";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "frame/" + file1, file1);
        var filename = "asamplep-1127-0001";
        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .combined(true)
                .storedDir(TEST_DIRECTORY.toString())
                .fileExtension("tif")
                .build();
        assertThat(ImageUtils.createThumbnail(frame)).isTrue();
    }

    @Test
    void createThumbnailHDR() {
        var file1 = "asamplep-ee32-0001.hdr";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "frame/" + file1, file1);
        var filename = "asamplep-ee32-0001";
        var frame = Frame.builder()
                .frameName(filename)
                .frameNumber(1)
                .combined(true)
                .storedDir(TEST_DIRECTORY.toString())
                .fileExtension("hdr")
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
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-4d6e-0001")
                .fileExtension("png")
                .partsPerFrame(4)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine9PartsPNGBarcelone() {
        var file1 = "png-9-parts-barcelone.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-2a7f-0001")
                .fileExtension("png")
                .partsPerFrame(9)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine4PartsHDR() {
        var file1 = "hdr-4-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-4d6e-0001")
                .fileExtension("hdr")
                .partsPerFrame(4)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();

    }

    @Test
    void combine9PartsHDR() {
        var file1 = "hdr-9-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-4d6e-0001")
                .fileExtension("hdr")
                .partsPerFrame(9)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();
    }

    @Test
    void combine9PartsHDRBarcelone() {
        var file1 = "hdr-9-parts-barcelone.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-2a7f-0001")
                .fileExtension("hdr")
                .partsPerFrame(9)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.HDR)).isTrue();
    }

    @Test
    void combine9PartsPNG() {
        var file1 = "png-9-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-4d6e-0001")
                .fileExtension("png")
                .partsPerFrame(9)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.PNG)).isTrue();

    }

    @Test
    void combine4PartsTIFF() {
        var file1 = "tiff-4-parts.zip";
        var partLocation = new File(TEST_DIRECTORY + File.separator + "frames" + File.separator + "parts");
        partLocation.mkdirs();
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "image_parts/" + file1, file1);
        FileUtils.extractArchive(TEST_DIRECTORY + File.separator + file1, partLocation.toString());
        var frame = Frame.builder()
                .frameName("asamplep-4d6e-0001")
                .fileExtension("tif")
                .partsPerFrame(4)
                .storedDir(TEST_DIRECTORY + File.separator + "frames")
                .combined(false)
                .frameNumber(1)
                .build();
        assertThat(ImageUtils.combineParts(frame, ImageOutputFormat.TIFF)).isTrue();
    }


    @Test
    void configureFrameList() {
        var project = TestUtils.getProject();
        var user = TestUtils.getUser(Stream.of(Role.USER).collect(Collectors.toSet()),
                "testuser1", "test1234$");
        project.setUser(user);
        var frameList = ImageUtils.configureFrameList(project);
        assertThat(frameList.size()).isEqualTo(project.getProjectSettings().getTotalNumberOfFrames());

    }
}
