package com.accounting.api.client;

import com.accounting.api.config.RestAssuredSetup;
import com.accounting.api.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Base API Client with common HTTP methods
 */
@Slf4j
public abstract class BaseApiClient {

    protected static final String BASE_URL = TestConfig.getBaseUrl();
    protected static final String API_PATH = TestConfig.getApiBasePath();

    // Shared session cookie for authenticated requests
    private static String sharedSessionCookie;

    public static void setSharedSessionCookie(String cookie) {
        sharedSessionCookie = cookie;
    }

    public static String getSharedSessionCookie() {
        return sharedSessionCookie;
    }

    protected RequestSpecification given() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().all();
    }

    protected RequestSpecification givenAuthenticated() {
        RequestSpecification spec = given();
        if (sharedSessionCookie != null && !sharedSessionCookie.isEmpty()) {
            spec.cookie("JSESSIONID", sharedSessionCookie);
        }
        return spec;
    }

    protected RequestSpecification givenForm() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.URLENC)
                .log().all();
    }

    protected RequestSpecification givenFormAuthenticated() {
        RequestSpecification spec = givenForm();
        if (sharedSessionCookie != null && !sharedSessionCookie.isEmpty()) {
            spec.cookie("JSESSIONID", sharedSessionCookie);
        }
        return spec;
    }

    // GET request
    protected Response get(String path) {
        log.info("GET request to: {}", path);
        return givenAuthenticated()
                .when()
                .get(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    protected Response get(String path, Map<String, ?> queryParams) {
        log.info("GET request to: {} with params: {}", path, queryParams);
        return givenAuthenticated()
                .queryParams(queryParams)
                .when()
                .get(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // POST request with JSON body
    protected Response post(String path, Object body) {
        log.info("POST request to: {} with body: {}", path, body);
        return givenAuthenticated()
                .body(body)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // POST request with form data
    protected Response postForm(String path, Map<String, ?> formParams) {
        log.info("POST form request to: {} with params: {}", path, formParams);
        return givenFormAuthenticated()
                .formParams(formParams)
                .when()
                .post(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // PUT request
    protected Response put(String path, Object body) {
        log.info("PUT request to: {} with body: {}", path, body);
        return givenAuthenticated()
                .body(body)
                .when()
                .put(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // DELETE request
    protected Response delete(String path) {
        log.info("DELETE request to: {}", path);
        return givenAuthenticated()
                .when()
                .delete(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    // PATCH request
    protected Response patch(String path, Object body) {
        log.info("PATCH request to: {} with body: {}", path, body);
        return givenAuthenticated()
                .body(body)
                .when()
                .patch(path)
                .then()
                .log().all()
                .extract()
                .response();
    }
}