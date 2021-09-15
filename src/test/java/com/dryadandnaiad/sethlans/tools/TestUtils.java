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

package com.dryadandnaiad.sethlans.tools;

import com.dryadandnaiad.sethlans.enums.*;
import com.dryadandnaiad.sethlans.models.blender.frames.Frame;
import com.dryadandnaiad.sethlans.models.blender.project.*;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.sessionId;

/**
 * File created by Mario Estrella on 12/27/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class TestUtils {

    public static User getUser(Set<Role> roles, String username, String password) {
        return User.builder()
                .active(true)
                .userID(UUID.randomUUID().toString())
                .username(username)
                .password(password)
                .roles(roles)
                .build();
    }

    public static Project getProject() {
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
                .startFrame(1)
                .endFrame(100)
                .stepFrame(1)
                .samples(50)
                .partsPerFrame(4)
                .totalNumberOfFrames(100)
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
                .projectID(UUID.randomUUID().toString())
                .projectName(RandomStringUtils.randomAlphabetic(40))
                .projectRootDir("/root")
                .projectType(ProjectType.STILL_IMAGE)
                .projectSettings(projectSettings)
                .projectStatus(projectStatus)
                .thumbnailFileNames(thumbnailFiles)
                .frameFileNames(frameFiles)
                .frameList(frames)
                .build();
    }


    public static String hostWithoutDomainName(String baseHost) {
        int iend = baseHost.indexOf(".");
        if (iend != -1) {
            return "https://" + baseHost.substring(0, iend).toLowerCase();
        }
        return "https://" + baseHost.toLowerCase();
    }

    public static String loginGetCSRFToken(String username, String password) {
        log.info("Starting login using username: " + username.toLowerCase() + ", " + " password: " + password);
        var response =
                given().
                        when().get("/login").
                        then().extract().response();
        String token =  response.cookie("XSRF-TOKEN");

        response = given().log().ifValidationFails()
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .param("username", username.toLowerCase())
                .param("password", password)
                .when().post("/login").then().statusCode(302).extract().response();

        sessionId = response.cookie("JSESSIONID");
        log.info("Login completed, obtained the following cookies");
        log.info("XSRF-TOKEN: " + token);
        log.info("JSESSIONID: " + sessionId);

        return token;
    }

    public static SetupForm setupDual(SetupForm setupForm) {
        var blenderVersions = setupForm.getBlenderVersions();

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if(setupForm.getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.add(setupForm.getAvailableGPUs().get(0));
        }

        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
                .response("Test").build();

        var serverSettings = ServerSettings.builder()
                .blenderVersion(blenderVersions.get(0))
                .build();

        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();

        var nodeSettings = NodeSettings.builder()
                .nodeType(nodeType)
                .cores(2)
                .tileSizeCPU(16)
                .tileSizeGPU(256)
                .selectedGPUs(selectedGPUs)
                .gpuCombined(false)
                .build();

        var user = User.builder()
                .username("testuser")
                .password("testPa$$1234")
                .active(true)
                .challengeList(List.of(challenge))
                .email("testuser@test.com")
                .roles(new HashSet<>(List.of(Role.SUPER_ADMINISTRATOR))).build();

        setupForm.setMode(SethlansMode.DUAL);
        setupForm.setLogLevel(LogLevel.DEBUG);
        setupForm.setServerSettings(serverSettings);
        setupForm.setMailSettings(mailSettings);
        setupForm.setNodeSettings(nodeSettings);
        setupForm.setUser(user);
        return setupForm;
    }

    public static SetupForm setupNode(SetupForm setupForm) {

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if(setupForm.getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.add(setupForm.getAvailableGPUs().get(0));
        }

        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
                .response("Test").build();

        var nodeSettings = NodeSettings.builder()
                .nodeType(nodeType)
                .cores(2)
                .tileSizeCPU(16)
                .tileSizeGPU(256)
                .selectedGPUs(selectedGPUs)
                .gpuCombined(false)
                .build();

        var user = User.builder()
                .username("testuser")
                .password("testPa$$1234")
                .active(true)
                .challengeList(List.of(challenge))
                .email("testuser@test.com")
                .roles(new HashSet<>(List.of(Role.SUPER_ADMINISTRATOR))).build();

        setupForm.setMode(SethlansMode.NODE);
        setupForm.setLogLevel(LogLevel.DEBUG);
        setupForm.setNodeSettings(nodeSettings);
        setupForm.setUser(user);
        return setupForm;
    }

    public static SetupForm setupServer(SetupForm setupForm) {
        var blenderVersions = setupForm.getBlenderVersions();

        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
                .response("Test").build();

        var serverSettings = ServerSettings.builder()
                .blenderVersion(blenderVersions.get(0))
                .build();

        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();

        var user = User.builder()
                .username("testuser")
                .password("testPa$$1234")
                .active(true)
                .challengeList(List.of(challenge))
                .email("testuser@test.com")
                .roles(new HashSet<>(List.of(Role.SUPER_ADMINISTRATOR))).build();

        setupForm.setMode(SethlansMode.SERVER);
        setupForm.setLogLevel(LogLevel.DEBUG);
        setupForm.setServerSettings(serverSettings);
        setupForm.setMailSettings(mailSettings);
        setupForm.setUser(user);
        return setupForm;
    }
}