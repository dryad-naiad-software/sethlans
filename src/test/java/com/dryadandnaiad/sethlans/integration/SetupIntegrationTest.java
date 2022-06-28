package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.user.SethlansUser;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
public class SetupIntegrationTest {


    @BeforeAll
    public static void setup() throws FileNotFoundException {

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

        log.info("Starting Setup Test on " + baseHost + ":" + RestAssured.port);

    }


    @Test
    public void test_full_setup() throws JsonProcessingException, InterruptedException {
        var mapper = new ObjectMapper();
        log.info("Check if first_time is true");
        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/info/is_first_time")
                .then()
                .statusCode(StatusCodes.OK)
                .assertThat()
                .body("first_time", equalTo(true));

        log.info("Getting Setup Form");

        var setupFormJson = get("/api/v1/setup/get_setup")
                .then()
                .extract()
                .response()
                .body()
                .asString();

        log.info(setupFormJson);

        var setupForm = mapper
                .readValue(setupFormJson, SetupForm.class);

        log.info("Retrieved Setup Form: \n" + setupForm);

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if (setupForm.getNodeSettings().getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.add(setupForm.getNodeSettings().getAvailableGPUs().get(0));
        }

        var blenderVersions = setupForm.getBlenderVersions();

        var challenge = UserChallenge.builder()
                .challenge("Question")
                .response("Test").build();

        var serverSettings = ServerSettings.builder()
                .blenderVersion(blenderVersions.get(0))
                .build();

        var nodeSettings = NodeSettings.builder()
                .nodeType(nodeType)
                .cores(2)
                .tileSizeCPU(16)
                .tileSizeGPU(256)
                .selectedGPUs(selectedGPUs)
                .gpuCombined(false)
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

        setupForm.setMode(SethlansMode.DUAL);
        setupForm.setServerSettings(serverSettings);
        setupForm.setNodeSettings(nodeSettings);
        setupForm.setMailSettings(mailSettings);
        setupForm.setSethlansUser(user);

        log.info("Submitting Setup Form \n" + setupForm);
        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(setupForm))
                .post("/api/v1/setup/submit").then().statusCode(StatusCodes.CREATED);

        log.info("Restarting Sethlans");

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/setup/restart")
                .then()
                .statusCode(StatusCodes.OK);

        log.info("Waiting 10 seconds");

        Thread.sleep(10000);

        TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        log.info("Verifying that first_time is false");
        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/info/is_first_time")
                .then()
                .statusCode(StatusCodes.OK)
                .assertThat()
                .body("first_time", equalTo(false));

        log.info("Verifying that Sethlans Mode is DUAL");
        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/info/mode")
                .then()
                .statusCode(StatusCodes.OK)
                .assertThat()
                .body("mode", equalTo("DUAL"));

        var jsonPath = given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/server_api_key")
                .then()
                .extract().response().jsonPath();

        String apiKey = jsonPath.get("api_key");
        log.info(apiKey);


        log.info("Obtaining Node Info");
        var nodeInfo = mapper
                .readValue(get("/api/v1/info/node_info?api-key=" + apiKey)
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), Node.class);

        assertThat(nodeInfo, notNullValue());
        log.info(nodeInfo.toString());

    }

    @AfterAll
    public static void shutdown() throws InterruptedException {
        var response = given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/shutdown");

        Assertions.assertThat(response.getStatusCode()).isGreaterThanOrEqualTo(200).isLessThan(300);
        Thread.sleep(10000);

        FileSystemUtils.deleteRecursively( new File(SystemUtils.USER_HOME + File.separator + ".sethlans"));
        Thread.sleep(5000);
    }

}
