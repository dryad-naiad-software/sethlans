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

import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.project.ImageSettings;
import com.dryadandnaiad.sethlans.models.blender.project.Project;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectSettings;
import com.dryadandnaiad.sethlans.models.blender.project.VideoSettings;
import com.dryadandnaiad.sethlans.testutils.TestFileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 5/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class FFmpegUtilsTest {

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
    void copyFFmpegArchiveToDisk() {
        var result = FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.LINUX_64);
        assertThat(result).isTrue();
        result = FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.UNSUPPORTED);
        assertThat(result).isFalse();
        result = FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_32);
        assertThat(result).isFalse();
    }

    @Test
    void installFFmpegWindows() {
        assertThat(FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_64)).isTrue();
        assertThat(FFmpegUtils.installFFmpeg(TEST_DIRECTORY.toString(), OS.WINDOWS_64)).isTrue();
    }

    @Test
    void installFFmpegLinux() {
        assertThat(FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.LINUX_64)).isTrue();
        assertThat(FFmpegUtils.installFFmpeg(TEST_DIRECTORY.toString(), OS.LINUX_64)).isTrue();
    }

    @Test
    void installFFmpegMac() {
        assertThat(FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.MACOS)).isTrue();
        assertThat(FFmpegUtils.installFFmpeg(TEST_DIRECTORY.toString(), OS.MACOS)).isTrue();
    }

    @Test
    void installFFmpegUnsupported() {
        assertThat(FFmpegUtils.copyFFmpegArchiveToDisk(TEST_DIRECTORY.toString(), OS.WINDOWS_32)).isFalse();
        assertThat(FFmpegUtils.installFFmpeg(TEST_DIRECTORY.toString(), OS.WINDOWS_32)).isFalse();
    }

    @Test
    void encodeImagesToMP4() {
        var projectDir = new File(TEST_DIRECTORY + File.separator + "project");
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        var videoDirectory = new File(projectDir + File.separator + "video");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        var videoSettings = project.getProjectSettings().getVideoSettings();
        videoSettings.setVideoFileLocation(videoDirectory + File.separator + QueryUtils.truncatedProjectNameAndID(project.getProjectName(),
                project.getProjectID()) + "." +
                VideoOutputFormat.MP4.name().toLowerCase());
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isTrue();
    }

    @Test
    void encodeImagesToAVI() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        var projectDir = new File(TEST_DIRECTORY + File.separator + "project");
        var videoDirectory = new File(projectDir + File.separator + "video");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        var videoSettings = project.getProjectSettings().getVideoSettings();
        videoSettings.setCodec(VideoCodec.UTVIDEO);
        videoSettings.setVideoOutputFormat(VideoOutputFormat.AVI);
        videoSettings.setVideoFileLocation(videoDirectory + File.separator + QueryUtils.truncatedProjectNameAndID(project.getProjectName(),
                project.getProjectID()) + "." +
                VideoOutputFormat.AVI.name().toLowerCase());
        project.getProjectSettings().setVideoSettings(videoSettings);
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isTrue();
    }

    @Test
    void encodeImagesToMKV() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        var projectDir = new File(TEST_DIRECTORY + File.separator + "project");
        var videoDirectory = new File(projectDir + File.separator + "video");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        var videoSettings = project.getProjectSettings().getVideoSettings();
        videoSettings.setCodec(VideoCodec.LIBX265);
        videoSettings.setVideoQuality(VideoQuality.HIGH_X265);
        videoSettings.setVideoOutputFormat(VideoOutputFormat.MKV);
        videoSettings.setVideoFileLocation(videoDirectory + File.separator +
                QueryUtils.truncatedProjectNameAndID(project.getProjectName(),
                        project.getProjectID()) + "." +
                VideoOutputFormat.MKV.name().toLowerCase());
        project.getProjectSettings().setVideoSettings(videoSettings);
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isTrue();
    }

    @Test
    void invalidEncodeMismatchedImage() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        project.getProjectSettings().getImageSettings().setImageOutputFormat(ImageOutputFormat.TIFF);
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isFalse();
    }

    @Test
    void invalidEncodeMissingFile() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        var filename = "test";
        project.getFrameFileNames().set(0, filename);
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isFalse();
    }

    @Test
    void invalidEncodeEmptyFileList() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        project.setFrameFileNames(new ArrayList<>());
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isFalse();
    }

    @Test
    void invalidEncodeFileLocation() {
        var binaryDirectory = new File(TEST_DIRECTORY + File.separator + "binaries");
        binaryDirectory.mkdirs();
        FFmpegUtils.copyFFmpegArchiveToDisk(binaryDirectory.toString(), QueryUtils.getOS());
        FFmpegUtils.installFFmpeg(binaryDirectory.toString(), QueryUtils.getOS());
        var ffmpegDirectory = new File(binaryDirectory + File.separator + "ffmpeg");
        var project = createProject();
        project.getProjectSettings().getVideoSettings().setVideoFileLocation("test");
        assertThat(FFmpegUtils.encodeImagesToVideo(project, ffmpegDirectory.toString())).isFalse();
    }


    private Project createProject() {
        var projectDir = new File(TEST_DIRECTORY + File.separator + "project");
        var videoDirectory = new File(projectDir + File.separator + "video");
        var imageDir = new File(projectDir + File.separator + "images");
        projectDir.mkdirs();
        imageDir.mkdirs();
        videoDirectory.mkdirs();
        var archive = "movie.tar.xz";
        TestFileUtils.copyTestArchiveToDisk(TEST_DIRECTORY.toString(), "movie/" + archive, archive);
        FileUtils.extractArchive(TEST_DIRECTORY.toString() + File.separator + archive,
                imageDir.toString());
        var initialList = FileUtils.listFiles(imageDir.toString());
        var imageList = new ArrayList<String>();
        for (String file : initialList) {
            imageList.add(imageDir + File.separator + file);
        }
        var imageSettings = ImageSettings.builder()
                .imageOutputFormat(ImageOutputFormat.PNG)
                .build();
        var videoSettings = VideoSettings.builder()
                .codec(VideoCodec.LIBX264)
                .videoOutputFormat(VideoOutputFormat.MP4)
                .frameRate(30)
                .videoQuality(VideoQuality.HIGH_X264)
                .pixelFormat(PixelFormat.YUV420P)
                .build();
        var projectSettings = ProjectSettings.builder()
                .videoSettings(videoSettings)
                .animationType(AnimationType.MOVIE)
                .imageSettings(imageSettings)
                .build();
        return Project.builder()
                .projectID("123e4567-e89b-12d3-a456-426655440000")
                .projectName("The Sample Project")
                .projectType(ProjectType.ANIMATION)
                .projectSettings(projectSettings)
                .projectRootDir(projectDir.toString())
                .frameFileNames(imageList)
                .build();
    }

}
