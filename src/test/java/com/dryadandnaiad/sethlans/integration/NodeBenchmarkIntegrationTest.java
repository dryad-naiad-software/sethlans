package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.models.forms.NodeForm;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.system.Server;
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
public class NodeBenchmarkIntegrationTest {
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

        log.info("Waiting 10 seconds");

        Thread.sleep(10000);

        log.info("Starting Node Benchmark Test on " + baseHost + ":" + RestAssured.port);

    }

    @Test
    public void node_benchmark_test() throws JsonProcessingException, InterruptedException {
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");
        var mapper = new ObjectMapper();
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

        for (Node node : nodeSet) {
            if (node.getHostname().equalsIgnoreCase(System.getProperty("sethlans.host"))) {
                var nodeForm = NodeForm.builder()
                        .ipAddress(node.getIpAddress())
                        .networkPort(node.getNetworkPort())
                        .username("testuser")
                        .password("testPa$$1234").build();
                nodeList.add(nodeForm);
            }

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


        assertThat(nodesOnServer.get(0).getHostname().toLowerCase()).contains(System.getProperty("sethlans.host").toLowerCase());
        log.info("Added the following node to server:");
        log.info(nodesOnServer.toString());

        var serversOnNode =  mapper
                .readValue(get("/api/v1/management/list_servers_on_node")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), new TypeReference<List<Server>>() {
                });

        assertThat(serversOnNode.get(0).getHostname().toLowerCase()).contains(System.getProperty("sethlans.host").toLowerCase());
        log.info("Confirmed server is present on node:");
        log.info(serversOnNode.toString());

        Thread.sleep(60000);
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

        Thread.sleep(30000);


    }

    @AfterAll
    public static void shutdown() throws InterruptedException {
        var response = given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/shutdown");

        assertThat(response.getStatusCode()).isGreaterThanOrEqualTo(200).isLessThan(300);
        Thread.sleep(10000);

        FileSystemUtils.deleteRecursively( new File(SystemUtils.USER_HOME + File.separator + ".sethlans"));
        Thread.sleep(5000);
    }


}
