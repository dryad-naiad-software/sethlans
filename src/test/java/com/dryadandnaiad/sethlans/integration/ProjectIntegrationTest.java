package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ProjectIntegrationTest {

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

        log.info("Starting Project Test on " + baseHost + ":" + RestAssured.port);

    }

    @Test
    public void create_project_test() {

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
