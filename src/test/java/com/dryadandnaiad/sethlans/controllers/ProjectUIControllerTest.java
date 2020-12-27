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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.project.*;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.NodeRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * File created by Mario Estrella on 12/25/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@ActiveProfiles("DUAL")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:sethlans.properties")
@AutoConfigureMockMvc
@DirtiesContext
class ProjectUIControllerTest {

    static File SETHLANS_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");

    @Autowired
    private MockMvc mvc;

    @Resource
    NodeRepository nodeRepository;


    @BeforeAll
    static void beforeAll() throws Exception {
        var setupSettings = SetupForm.builder()
                .appURL("https://localhost:7443")
                .ipAddress(QueryUtils.getIP())
                .logLevel(LogLevel.DEBUG)
                .mode(SethlansMode.DUAL)
                .port("7443").build();
        var nodeSettings = NodeSettings.builder().nodeType(NodeType.CPU).tileSizeCPU(32).cores(4).build();
        PropertiesUtils.writeNodeSettings(nodeSettings);
        PropertiesUtils.writeSetupSettings(setupSettings);
        PropertiesUtils.writeDirectories(SethlansMode.DUAL);
        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();
        PropertiesUtils.writeMailSettings(mailSettings);

    }

    @AfterAll
    static void afterAll() {
        FileSystemUtils.deleteRecursively(SETHLANS_DIRECTORY);
    }

    @Test
    void nodesReady() throws Exception {
        var nodeList = new ArrayList<Node>();
        nodeList.add(Node.builder()
                .ipAddress("10.10.10.10")
                .networkPort("7443")
                .active(true)
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .build());
        nodeList.add(Node.builder()
                .ipAddress("10.10.10.1")
                .networkPort("7443")
                .nodeType(NodeType.CPU)
                .systemID(UUID.randomUUID().toString())
                .active(true)
                .build());
        for (Node node : nodeList) {
            nodeRepository.save(node);
        }

        var result = mvc.perform(get("/api/v1/project/nodes_ready")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk()).andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("true");
    }

    @Test
    @WithMockUser(username = "testuser", password = "test1234", roles = "USER")
    void getProjects() {
        //TODO
    }

    User getUser() {
        return User.builder()
                .active(true)
                .id(12345L)
                .userID(UUID.randomUUID().toString())
                .username("testuser")
                .password("test1234")
                .build();
    }

    Project getProject() {
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


        return Project.builder()
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
                .user(getUser())
                .build();
    }


}
