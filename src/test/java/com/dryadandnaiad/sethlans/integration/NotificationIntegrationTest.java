package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.AnimationType;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.system.Notification;
import com.dryadandnaiad.sethlans.tools.TestFileUtils;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class NotificationIntegrationTest {
    static File TEST_DIRECTORY = new File(SystemUtils.USER_HOME + File.separator + ".sethlans");
    static File BLEND_DIRECTORY = new File(TEST_DIRECTORY.toString() + File.separator + "blend_files/");


    @BeforeAll
    public static void setup() throws FileNotFoundException, JsonProcessingException, InterruptedException {

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

        log.info("Waiting 20 seconds");

        Thread.sleep(10000);

        BLEND_DIRECTORY.mkdirs();

        var file1 = "wasp_bot.blend";
        var file2 = "refract_monkey.blend";
        var file3 = "bmw27_gpu.blend";
        var file4 = "scene-helicopter-27.blend";
        var file5 = "pavillon_barcelone_v1.2.zip";

        TestFileUtils.copyTestArchiveToDisk(BLEND_DIRECTORY.toString(), "blend_files/" + file1, file1);
        TestFileUtils.copyTestArchiveToDisk(BLEND_DIRECTORY.toString(), "blend_files/" + file2, file2);
        TestFileUtils.copyTestArchiveToDisk(BLEND_DIRECTORY.toString(), "blend_files/" + file3, file3);
        TestFileUtils.copyTestArchiveToDisk(BLEND_DIRECTORY.toString(), "blend_files/" + file4, file4);
        TestFileUtils.copyTestArchiveToDisk(BLEND_DIRECTORY.toString(), "blend_files/" + file5, file5);

        log.info("Starting Project Test on " + baseHost + ":" + RestAssured.port);

    }

    @AfterAll
    public static void shutdown() throws InterruptedException {
        var response = given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/shutdown");

        assertThat(response.getStatusCode()).isGreaterThanOrEqualTo(200).isLessThan(300);
        Thread.sleep(10000);

        FileSystemUtils.deleteRecursively(new File(SystemUtils.USER_HOME + File.separator + ".sethlans"));
        Thread.sleep(5000);
    }

    @Test
    public void check_notifications() throws JsonProcessingException, InterruptedException {
        var mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        log.info("Waiting for Blender download(s) to complete.");


        var downloadState = Boolean.parseBoolean(given().when().get("/api/v1/management/blender_download_complete")
                .then()
                .extract()
                .response()
                .body()
                .asString());

        while (!downloadState) {
            Thread.sleep(5000);
            downloadState = Boolean.parseBoolean(given().when().get("/api/v1/management/blender_download_complete")
                    .then()
                    .extract()
                    .response()
                    .body()
                    .asString());
        }

        log.info("Blender download(s) complete.");

        var response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/refract_monkey.blend"))
                .accept(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .post("/api/v1/project/upload_project_file")
                .then()
                .statusCode(StatusCodes.CREATED)
                .extract()
                .response()
                .body()
                .asString();

        var projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);

        response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/bmw27_gpu.blend"))
                .accept(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .post("/api/v1/project/upload_project_file")
                .then()
                .statusCode(StatusCodes.CREATED)
                .extract()
                .response()
                .body()
                .asString();

        projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);

        response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/scene-helicopter-27.blend"))
                .accept(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .post("/api/v1/project/upload_project_file")
                .then()
                .statusCode(StatusCodes.CREATED)
                .extract()
                .response()
                .body()
                .asString();

        projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);

        response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/wasp_bot.blend"))
                .accept(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .post("/api/v1/project/upload_project_file")
                .then()
                .statusCode(StatusCodes.CREATED)
                .extract()
                .response()
                .body()
                .asString();

        projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);

        response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/wasp_bot.blend"))
                .accept(ContentType.JSON)
                .contentType(ContentType.MULTIPART)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .post("/api/v1/project/upload_project_file")
                .then()
                .statusCode(StatusCodes.CREATED)
                .extract()
                .response()
                .body()
                .asString();

        projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());
        projectForm.getProjectSettings().setAnimationType(AnimationType.IMAGES);
        projectForm.setProjectType(ProjectType.ANIMATION);

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);

        var notifications = mapper
                .readValue(get("/api/v1/notifications/get_notifications")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<Notification>>() {
                });

        log.info(notifications.toString());

        var notification = notifications.get(1);

        given()
                .log()
                .ifValidationFails()
                .param("notificationID", notification.getNotificationID())
                .post("/api/v1/notifications/mark_read")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        notifications = mapper
                .readValue(get("/api/v1/notifications/get_notifications")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<Notification>>() {
                });

        notification = notifications.get(1);

        assertThat(notification.isMessageRead()).isEqualTo(true);

    }
}
