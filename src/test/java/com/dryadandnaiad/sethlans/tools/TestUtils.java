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
import com.dryadandnaiad.sethlans.models.blender.project.*;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.util.*;

import static io.restassured.RestAssured.*;

/**
 * File created by Mario Estrella on 12/27/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class TestUtils {

    public static SethlansUser getUser(Set<Role> roles, String username, String password) {
        return SethlansUser.builder()
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
                .videoSettings(videoSettings)
                .imageSettings(imageSettings)
                .build();

        var projectStatus = ProjectStatus.builder()
                .projectState(ProjectState.FINISHED)
                .allImagesProcessed(true)
                .queueIndex(1)
                .currentPercentage(50)
                .totalQueueSize(34)
                .reEncode(false)
                .completedFrames(34)
                .totalRenderTime(123L)
                .totalProjectTime(123L)
                .remainingQueueSize(123)
                .timerStart(123L)
                .timerEnd(123L)
                .build();


        return Project.builder()
                .projectID(UUID.randomUUID().toString())
                .projectName(RandomStringUtils.randomAlphabetic(40))
                .projectRootDir("/root")
                .projectType(ProjectType.STILL_IMAGE)
                .projectSettings(projectSettings)
                .projectStatus(projectStatus)
                .build();
    }

    public static void copyBlendFilesForTest(File blendDirectory) {
        blendDirectory.mkdirs();

        var file1 = "wasp_bot.blend";
        var file2 = "refract_monkey.blend";
        var file3 = "bmw27_gpu.blend";
        var file4 = "scene-helicopter-27.blend";
        var file5 = "pavillon_barcelone_v1.2.zip";

        TestFileUtils.copyTestArchiveToDisk(blendDirectory.toString(), "blend_files/" + file1, file1);
        TestFileUtils.copyTestArchiveToDisk(blendDirectory.toString(), "blend_files/" + file2, file2);
        TestFileUtils.copyTestArchiveToDisk(blendDirectory.toString(), "blend_files/" + file3, file3);
        TestFileUtils.copyTestArchiveToDisk(blendDirectory.toString(), "blend_files/" + file4, file4);
        TestFileUtils.copyTestArchiveToDisk(blendDirectory.toString(), "blend_files/" + file5, file5);
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

    public static void setupDual() throws JsonProcessingException, InterruptedException {
        var port = System.getProperty("sethlans.port");
        if (port == null) {
            RestAssured.port = 7443;
        } else {
            RestAssured.port = Integer.parseInt(port);
        }

        var basePath = System.getProperty("sethlans.base");
        if (basePath == null) {
            basePath = "/";
        }
        RestAssured.basePath = basePath;

        var baseHost = System.getProperty("sethlans.host");
        if (baseHost == null) {
            baseHost = "https://localhost";
        } else {
            baseHost = hostWithoutDomainName(baseHost);
        }
        RestAssured.baseURI = baseHost;
        RestAssured.useRelaxedHTTPSValidation();

        log.info("Preparing system for test");
        var mapper = new ObjectMapper();

        var setupForm = mapper
                .readValue(get("/api/v1/setup/get_setup")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), SetupForm.class);

        setupForm = TestUtils.prepareSetupFormDual(setupForm);

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(setupForm))
                .post("/api/v1/setup/submit")
                .then()
                .statusCode(StatusCodes.CREATED);

        log.info("Restarting Sethlans");

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/setup/restart")
                .then()
                .statusCode(StatusCodes.OK);
        log.info("Waiting 10 seconds for Restart to complete");

        Thread.sleep(10000);
    }

    public static SetupForm prepareSetupFormDual(SetupForm setupForm) {
        var blenderVersions = setupForm.getBlenderVersions();

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if (setupForm.getNodeSettings().getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.addAll(setupForm.getNodeSettings().getAvailableGPUs());
            selectedGPUs.removeIf(gpu -> gpu.getModel().contains("Vega 8 Graphics"));
        }

        var challenge = UserChallenge.builder()
                .challenge("Test1234")
                .response("Test").build();

        var serverSettings = ServerSettings.builder()
                .blenderVersion(blenderVersions.get(0))
                .build();

        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();

        var nodeSettings = NodeSettings.builder()
                .nodeType(nodeType)
                .cores(setupForm.getSystemInfo().getCpu().getCores() / 2)
                .tileSizeCPU(16)
                .tileSizeGPU(256)
                .selectedGPUs(selectedGPUs)
                .gpuCombined(false)
                .build();

        var user = SethlansUser.builder()
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
        setupForm.setSethlansUser(user);
        return setupForm;
    }

    public static SetupForm prepareSetupFormNode(SetupForm setupForm) {

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if (setupForm.getNodeSettings().getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.add(setupForm.getNodeSettings().getAvailableGPUs().get(0));
        }

        var challenge = UserChallenge.builder()
                .challenge("Test1234")
                .response("Test").build();

        var nodeSettings = NodeSettings.builder()
                .nodeType(nodeType)
                .cores(2)
                .tileSizeCPU(16)
                .tileSizeGPU(256)
                .selectedGPUs(selectedGPUs)
                .gpuCombined(false)
                .build();

        var user = SethlansUser.builder()
                .username("testuser")
                .password("testPa$$1234")
                .active(true)
                .challengeList(List.of(challenge))
                .email("testuser@test.com")
                .roles(new HashSet<>(List.of(Role.SUPER_ADMINISTRATOR))).build();

        setupForm.setMode(SethlansMode.NODE);
        setupForm.setLogLevel(LogLevel.DEBUG);
        setupForm.setNodeSettings(nodeSettings);
        setupForm.setSethlansUser(user);
        return setupForm;
    }

    public static SetupForm prepareSetupFormServer(SetupForm setupForm) {
        var blenderVersions = setupForm.getBlenderVersions();

        var challenge = UserChallenge.builder()
                .challenge("Test1234")
                .response("Test").build();

        var serverSettings = ServerSettings.builder()
                .blenderVersion(blenderVersions.get(0))
                .build();

        var mailSettings = MailSettings.builder()
                .mailEnabled(false)
                .build();

        var user = SethlansUser.builder()
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
        setupForm.setSethlansUser(user);
        return setupForm;
    }

    public static String titleGenerator(){
        ArrayList<String> titles = new ArrayList<>(Arrays.asList(
                "Lorem ipsum dolor sit amet",
                "Est consequuntur corporis sed internos",
                "Esse et obcaecati itaque eum nemo amet",
                "Nam dolor ducimus qui galisum deserunt",
                "Et consectetur omnis vel voluptatem",
                "Sed perspiciatis pariatur in sunt sunt id molestias dolores",
                "Tenetur ea dolore rerum qui magni quisquam"));

        var random = new Random();

        return titles.get(random.nextInt(titles.size()));


    }
}
