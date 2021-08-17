package com.dryadandnaiad.sethlans.integration;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.forms.SetupForm;
import com.dryadandnaiad.sethlans.models.settings.MailSettings;
import com.dryadandnaiad.sethlans.models.settings.ServerSettings;
import com.dryadandnaiad.sethlans.models.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.dryadandnaiad.sethlans.testutils.TestUtils.commentGenerator;
import static com.dryadandnaiad.sethlans.testutils.TestUtils.hostWithoutDomainName;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

public class SetupIntegratedTest {
    private String token;


    @BeforeAll
    public static void setup() {
        commentGenerator("Starting Server Setup Test");

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

    }

    @Test
    public void test_server_setup() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        commentGenerator("Verifying that Sethlans Setup is active");
        given().log().ifValidationFails().get("/api/v1/info/is_first_time").then().
                statusCode(200).assertThat().body("first_time", equalTo(true));


        var setupForm = mapper.readValue(get("/api/v1/setup/get_setup").then().extract().response().body().asString(), SetupForm.class);

        var blenderVersions = setupForm.getBlenderVersions();

        var availableGPUs = setupForm.getAvailableGPUs();

        var nodeTypes = setupForm.getAvailableTypes();

        var serverSettings = ServerSettings.builder().blenderVersion(blenderVersions.get(0)).build();

        var mailSettings = MailSettings.builder().mailEnabled(false).build();

        var user = User.builder().
                username("testuser").
                password("testPa$$1234").
                active(true).
                email("testuser@test.com").
                roles(new HashSet<>(List.of(Role.SUPER_ADMINISTRATOR))).build();

        setupForm.setMode(SethlansMode.SERVER);
        setupForm.setServerSettings(serverSettings);
        setupForm.setMailSettings(mailSettings);
        setupForm.setUser(user);


        System.out.println(setupForm);
    }
}
