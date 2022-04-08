package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.AnimationType;
import com.dryadandnaiad.sethlans.enums.ProjectType;
import com.dryadandnaiad.sethlans.models.blender.project.ProjectView;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.models.forms.NodeForm;
import com.dryadandnaiad.sethlans.models.forms.ProjectForm;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
import com.dryadandnaiad.sethlans.tools.TestFileUtils;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ServerPendingQueueIntegrationTest {

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

        setupForm = TestUtils.setupDual(setupForm);

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

        log.info("Scanning for Nodes on the local network");
        var nodeSet = mapper
                .readValue(get("/api/v1/management/network_node_scan")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<Set<Node>>() {
                });


        log.info(nodeSet.toString());

        var nodeList = new ArrayList<NodeForm>();
        if(System.getProperty("sethlans.host") != null) {
            for (Node node : nodeSet) {
                if (node.getHostname().equalsIgnoreCase(System.getProperty("sethlans.host"))) {
                    var nodeForm = NodeForm.builder()
                            .ipAddress(node.getIpAddress())
                            .networkPort(node.getNetworkPort()).build();
                    nodeList.add(nodeForm);
                }
            }
        } else {
            var node = nodeSet.iterator().next();
            var nodeForm = NodeForm.builder()
                    .ipAddress(node.getIpAddress())
                    .networkPort(node.getNetworkPort()).build();
            nodeList.add(nodeForm);
        }


        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .body(mapper.writeValueAsString(nodeList))
                .post("/api/v1/management/add_nodes_to_server")
                .then()
                .statusCode(StatusCodes.CREATED);


        log.info("Waiting 10 seconds");

        Thread.sleep(10000);

        var nodesOnServer = mapper
                .readValue(get("/api/v1/management/current_node_list")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<Node>>() {
                });

        if(System.getProperty("sethlans.host") != null) {
            assertThat(nodesOnServer.get(0).getHostname().toLowerCase()).contains(System.getProperty("sethlans.host").toLowerCase());
        }
        log.info("Added the following node to server:");
        log.info(nodesOnServer.toString());

        var serversOnNode = mapper
                .readValue(get("/api/v1/management/list_servers_on_node")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<Server>>() {
                });

        if(System.getProperty("sethlans.host") != null) {
            assertThat(nodesOnServer.get(0).getHostname().toLowerCase()).contains(System.getProperty("sethlans.host").toLowerCase());
            log.info("Confirmed server is present on node:");
        }
        log.info(serversOnNode.toString());

        var params = ImmutableMap.<String, String>builder()
                .put("nodeID", nodesOnServer.get(0).getSystemID())
                .build();

        log.info(params.toString());

        var benchmarkState = Boolean.parseBoolean(given().params(params).when().get("/api/v1/management/node_benchmark_status")
                .then()
                .extract()
                .response()
                .body()
                .asString());

        log.info("Waiting for Benchmark(s) to complete");

        while (!benchmarkState) {
            Thread.sleep(10000);
            benchmarkState = Boolean.parseBoolean(given().params(params).when().get("/api/v1/management/node_benchmark_status")
                    .then()
                    .extract()
                    .response()
                    .body()
                    .asString());
        }

        log.info("Benchmark Complete");

        given()
                .log()
                .ifValidationFails()
                .param("pause", true)
                .post("/api/v1/management/pause_node")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        log.info("Starting Server Queue Test on " + baseHost + ":" + RestAssured.port);

    }

    @Test
    public void bmwStaticImage4Parts() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        var response = given()
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

        var projectForm = mapper.readValue(response, ProjectForm.class);
        projectForm.setProjectName(TestUtils.titleGenerator());
        projectForm.getProjectSettings().setStartFrame(1);
        projectForm.getProjectSettings().setPartsPerFrame(4);
        projectForm.getProjectSettings().setUseParts(true);

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(projectForm))
                .post("/api/v1/project/create_project")
                .then()
                .statusCode(StatusCodes.CREATED);


        given()
                .log()
                .ifValidationFails()
                .param("projectID", projectForm.getProjectID())
                .post("/api/v1/project/start_project")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        var project = mapper
                .readValue(get("/api/v1/project/" + projectForm.getProjectID())
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), ProjectView.class);

        var queue = mapper
                .readValue(get("/api/v1/management/view_server_pending_queue")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<RenderTask>>() {
                });

        log.info(project.toString());
        log.info(queue.toString());
        assertThat(queue.size()).isGreaterThanOrEqualTo(2);
        assertThat(project.getProjectStatus().getCurrentFrame()).isEqualTo(1);
        assertThat(project.getProjectStatus().getQueueIndex()).isGreaterThanOrEqualTo(2);
        assertThat(project.getProjectStatus().getCurrentPart()).isGreaterThanOrEqualTo(2);

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/reset_server_pending_queue")
                .then()
                .statusCode(StatusCodes.OK);
    }

    @Test
    public void pavillonBarceloneAnimation() throws JsonProcessingException, InterruptedException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        var response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/pavillon_barcelone_v1.2.zip"))
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
        projectForm.getProjectSettings().setStartFrame(1);
        projectForm.getProjectSettings().setEndFrame(50);
        projectForm.getProjectSettings().setUseParts(false);
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


        given()
                .log()
                .ifValidationFails()
                .param("projectID", projectForm.getProjectID())
                .post("/api/v1/project/start_project")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        var project = mapper
                .readValue(get("/api/v1/project/" + projectForm.getProjectID())
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), ProjectView.class);
        var queue = mapper
                .readValue(get("/api/v1/management/view_server_pending_queue")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<RenderTask>>() {
                });

        log.info(project.toString());
        log.info(queue.toString());
        assertThat(queue.size()).isGreaterThanOrEqualTo(2);
        assertThat(project.getProjectStatus().getCurrentFrame()).isGreaterThanOrEqualTo(2);
        assertThat(project.getProjectStatus().getQueueIndex()).isGreaterThanOrEqualTo(2);

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/reset_server_pending_queue")
                .then()
                .statusCode(StatusCodes.OK);
        Thread.sleep(10000);
    }

    @Test
    public void pavillonBarceloneAnimationSkip2() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        var response = given()
                .log()
                .ifValidationFails()
                .multiPart("project_file", new File(BLEND_DIRECTORY.toString() + "/pavillon_barcelone_v1.2.zip"))
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
        projectForm.getProjectSettings().setStartFrame(25);
        projectForm.getProjectSettings().setEndFrame(315);
        projectForm.getProjectSettings().setStepFrame(2);
        projectForm.getProjectSettings().setUseParts(false);
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


        given()
                .log()
                .ifValidationFails()
                .param("projectID", projectForm.getProjectID())
                .post("/api/v1/project/start_project")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        var project = mapper
                .readValue(get("/api/v1/project/" + projectForm.getProjectID())
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), ProjectView.class);
        var queue = mapper
                .readValue(get("/api/v1/management/view_server_pending_queue")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<RenderTask>>() {
                });

        log.info(project.toString());
        log.info(queue.toString());
        assertThat(queue.size()).isGreaterThanOrEqualTo(2);
        assertThat(queue.get(0).getFrameInfo().getFrameNumber()).isEqualTo(25);
        assertThat(queue.get(1).getFrameInfo().getFrameNumber()).isEqualTo(27);

        assertThat(project.getProjectStatus().getCurrentFrame()).isGreaterThanOrEqualTo(27);
        assertThat(project.getProjectStatus().getQueueIndex()).isGreaterThanOrEqualTo(2);

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/reset_server_pending_queue")
                .then()
                .statusCode(StatusCodes.OK);
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
}
