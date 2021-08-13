package com.dryadandnaiad.sethlans.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

import static com.dryadandnaiad.sethlans.testutils.TestUtils.commentGenerator;
import static com.dryadandnaiad.sethlans.testutils.TestUtils.hostWithoutDomainName;

public class SetupIntegratedTest {
    private String token;


    @BeforeAll
    public static void setup() {
        commentGenerator("Starting Server Setup Test");

        String port = System.getProperty("sethlans.port");
        if (port == null) {
            RestAssured.port = 7443;
        } else {
            RestAssured.port = Integer.parseInt(port);
        }


        String basePath = System.getProperty("sethlans.base");
        if (basePath == null) {
            basePath = "/";
        }
        RestAssured.basePath = basePath;

        String baseHost = System.getProperty("sethlans.host");
        if (baseHost == null) {
            baseHost = "https://localhost";
        } else {
            baseHost = hostWithoutDomainName(baseHost);
        }
        RestAssured.baseURI = baseHost;
        RestAssured.useRelaxedHTTPSValidation();

    }
}
