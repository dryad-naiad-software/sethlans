package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SecurityQuestion;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.models.user.UserChallenge;
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
import java.util.HashSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.tools.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class UserIntegrationTest {

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

        Thread.sleep(20000);

        log.info("Starting User Integration Tests on " + baseHost + ":" + RestAssured.port);

    }

    @Test
    public void test_user_list(){
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");
        var userList = get("/api/v1/management/user_list")
                .then()
                .extract()
                .response()
                .body()
                .asString();

        log.info(userList);

    }

    @Test
    public void test_update_user() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");
        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
                .response("Test").build();

        var user1 = User.builder()
                .username("NewU3ser24")
                .password("newPassWord1241")
                .challengeList(List.of(challenge))
                .email("cat@cat.com")
                .roles(new HashSet<>(List.of(Role.USER))).build();

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .body(mapper.writeValueAsString(user1))
                .post("/api/v1/management/create_user")
                .then()
                .statusCode(StatusCodes.CREATED);

        var user2 = User.builder()
                .username("newu3ser24")
                .password("newPassWSa2ord1241")
                .active(true)
                .challengeList(List.of(challenge))
                .email("ca2t@cat.com").build();

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .body(mapper.writeValueAsString(user2))
                .put("/api/v1/management/update_user")
                .then()
                .statusCode(StatusCodes.ACCEPTED);

        var params = ImmutableMap.<String, String>builder()
                .put("username", "newuser24")
                .build();

        log.info(given().params(params).when().get("/api/v1/management/get_user")
                .then()
                .extract()
                .response()
                .body()
                .asString());
    }

    @Test
    public void test_create_user() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");
        var challenge = UserChallenge.builder()
                .challenge(SecurityQuestion.QUESTION1)
                .response("Test").build();

        var user = User.builder()
                .username("NewUser24")
                .password("newPassWord1241")
                .challengeList(List.of(challenge))
                .email("cat@cat.com")
                .roles(new HashSet<>(List.of(Role.USER))).build();

        given()
                .log()
                .ifValidationFails()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .body(mapper.writeValueAsString(user))
                .post("/api/v1/management/create_user")
                .then()
                .statusCode(StatusCodes.CREATED);

    }

    @Test
    public void test_current_user() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var token = TestUtils.loginGetCSRFToken("testuser", "testPa$$1234");
        var currentUser =  mapper
                .readValue(get("/api/v1/management/get_current_user")
                .then()
                .extract()
                .response()
                .body()
                .asString(),new TypeReference<User>() {
                });

        assertThat(currentUser).isInstanceOf(User.class);
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
