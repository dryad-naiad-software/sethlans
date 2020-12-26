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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.project.*;
import com.dryadandnaiad.sethlans.models.user.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class ProjectToProjectViewTest {

    ProjectToProjectView projectToProjectView = new ProjectToProjectView();

    @Test
    void convert() {
        var videoSettings = VideoSettings.builder()
                .videoFileLocation("/home/testfile.mp4")
                .videoOutputFormat(VideoOutputFormat.MP4)
                .videoQuality(VideoQuality.HIGH_X264)
                .codec(VideoCodec.LIBX264)
                .frameRate(30)
                .pixelFormat(PixelFormat.YUV420P)
                .build();
        var imageSettings = ImageSettings.builder()
                .imageOutputFormat(ImageOutputFormat.PNG)
                .resolutionX(1980)
                .resolutionY(1024)
                .resPercentage(50)
                .build();
        var projectSettings = ProjectSettings.builder()
                .blenderEngine(BlenderEngine.CYCLES)
                .computeOn(ComputeOn.CPU)
                .blenderVersion("2.79b")
                .animationType(AnimationType.IMAGES)
                .startFrame(2)
                .endFrame(100)
                .stepFrame(1)
                .samples(50)
                .partsPerFrame(4)
                .totalNumberOfFrames(500)
                .useParts(true)
                .blendFilename("sampleblend.blend")
                .blendFilenameMD5Sum("dsafjaoif23548239")
                .blendFileLocation("/home")
                .videoSettings(videoSettings)
                .imageSettings(imageSettings)
                .build();

        var projectStatus = ProjectStatus.builder()
                .projectState(ProjectState.FINISHED)
                .allImagesProcessed(true)
                .queueIndex(1)
                .currentPercentage(50)
                .totalQueueSize(34)
                .userStopped(false)
                .queueFillComplete(true)
                .reEncode(false)
                .completedFrames(34)
                .totalRenderTime(123L)
                .totalProjectTime(123L)
                .remainingQueueSize(123)
                .timerStart(123L)
                .timerEnd(123L)
                .build();

        var thumbnailFiles = new ArrayList<String>();
        thumbnailFiles.add("test1234-1-thumb.png");
        thumbnailFiles.add("test1234-2-thumb.png");

        var frameFiles = new ArrayList<String>();
        frameFiles.add("test1234-1.png");
        frameFiles.add("test1234-2.png");

        var frames = new ArrayList<Frame>();

        var frame1 = Frame.builder()
                .frameNumber(1)
                .partsPerFrame(4)
                .frameName("test1234-1")
                .combined(true)
                .fileExtension("png")
                .storedDir("/temp")
                .build();
        var frame2 = Frame.builder()
                .frameNumber(2)
                .partsPerFrame(4)
                .frameName("test1234-2")
                .combined(true)
                .fileExtension("png")
                .storedDir("/temp")
                .build();

        frames.add(frame1);
        frames.add(frame2);

        var user =
                User.builder()
                        .active(true)
                        .id(12345L)
                        .userID(UUID.randomUUID().toString())
                        .username("testuser")
                        .password("test1234")
                        .build();

        var project = Project.builder()
                .id(12415L)
                .projectID(UUID.randomUUID().toString())
                .projectName("A Sample Project")
                .projectRootDir("/root")
                .projectType(ProjectType.STILL_IMAGE)
                .projectSettings(projectSettings)
                .projectStatus(projectStatus)
                .thumbnailFileNames(thumbnailFiles)
                .frameFileNames(frameFiles)
                .frameList(frames)
                .user(user)
                .build();

        var projectView = projectToProjectView.convert(project);
        for (Field f : projectView.getClass().getDeclaredFields()) {
            assertThat(f).isNotNull();
        }

    }
}
