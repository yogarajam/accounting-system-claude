package com.accounting.api.config;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * RestAssured Configuration
 * Sets up RestAssured with base configuration
 */
public class RestAssuredSetup {

    private static RequestSpecification requestSpec;
    private static ByteArrayOutputStream requestCapture;
    private static ByteArrayOutputStream responseCapture;

    public static void setup() {
        RestAssured.baseURI = TestConfig.getBaseUrl();
        RestAssured.basePath = TestConfig.getApiBasePath();

        // Configure logging
        requestCapture = new ByteArrayOutputStream();
        responseCapture = new ByteArrayOutputStream();

        RestAssured.config = RestAssuredConfig.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL));

        // Build request specification
        requestSpec = new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBaseUrl())
                .setBasePath(TestConfig.getApiBasePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL, new PrintStream(requestCapture)))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL, new PrintStream(responseCapture)))
                .build();
    }

    public static RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            setup();
        }
        return requestSpec;
    }

    public static RequestSpecification getAuthenticatedRequestSpec() {
        return getRequestSpec()
                .auth()
                .preemptive()
                .basic(TestConfig.getUsername(), TestConfig.getPassword());
    }

    public static RequestSpecification getFormRequestSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(TestConfig.getBaseUrl())
                .setContentType(ContentType.URLENC)
                .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                .build();
    }

    public static void reset() {
        RestAssured.reset();
        requestSpec = null;
    }

    public static String getLastRequestLog() {
        return requestCapture != null ? requestCapture.toString() : "";
    }

    public static String getLastResponseLog() {
        return responseCapture != null ? responseCapture.toString() : "";
    }
}