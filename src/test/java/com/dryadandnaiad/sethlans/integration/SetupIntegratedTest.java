package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SecurityQuestion;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.NodeSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
import com.dryadandnaiad.sethlans.tools.TestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
public class SetupIntegratedTest {


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
            //baseHost = "https://localhost";
            baseHost = "https://iota";
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

        var setupForm = mapper
                .readValue(get("/api/v1/setup/get_setup")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), SetupForm.class);

        log.info("Retrieved Setup Form: \n" + setupForm);

        var nodeType = NodeType.CPU;
        var selectedGPUs = new ArrayList<GPU>();

        if(setupForm.getAvailableGPUs().size() > 0) {
            nodeType = NodeType.CPU_GPU;
            selectedGPUs.add(setupForm.getAvailableGPUs().get(0));
        }

        var blenderVersions = setupForm.getBlenderVersions();

        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
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

        var user = User.builder()
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
        setupForm.setUser(user);

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(setupForm))
                .post("/api/v1/setup/submit");

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/setup/restart")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        Thread.sleep(10000);

        TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/info/is_first_time")
                .then()
                .statusCode(StatusCodes.OK)
                .assertThat()
                .body("first_time", equalTo(false));

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/info/mode")
                .then()
                .statusCode(StatusCodes.OK)
                .assertThat()
                .body("mode", equalTo("DUAL"));

        var nodeInfo = mapper
                .readValue(get("/api/v1/info/node_info")
                        .then()
                        .extract()
                        .response()
                        .body()
                        .asString(), Node.class);

        assertThat(nodeInfo, notNullValue());
        log.info(nodeInfo.toString());

        given()
                .log()
                .ifValidationFails()
                .get("/api/v1/management/shutdown")
                .then()
                .statusCode(StatusCodes.ACCEPTED);


    }
}
