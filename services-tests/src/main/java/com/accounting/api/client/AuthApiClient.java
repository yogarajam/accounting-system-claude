package com.accounting.api.client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authentication API Client
 * Handles login, logout, and session management
 */
@Slf4j
public class AuthApiClient extends BaseApiClient {

    private static final String LOGIN_PATH = "/login";
    private static final String LOGOUT_PATH = "/logout";
    private static final String DASHBOARD_PATH = "/dashboard";

    private String sessionCookie;
    private String csrfToken;

    public Response login(String username, String password) {
        log.info("Logging in as: {}", username);

        // First, get the login page to extract CSRF token and session cookie
        Response loginPage = RestAssured.given()
                .baseUri(BASE_URL)
                .when()
                .get(LOGIN_PATH)
                .then()
                .extract()
                .response();

        // Extract CSRF token from the login page
        String pageBody = loginPage.getBody().asString();
        csrfToken = extractCsrfToken(pageBody);
        String initialSession = loginPage.getCookie("JSESSIONID");

        log.info("Got CSRF token: {} and session: {}", csrfToken, initialSession);

        // Now submit the login form with CSRF token
        Map<String, String> formParams = new HashMap<>();
        formParams.put("username", username);
        formParams.put("password", password);
        formParams.put("_csrf", csrfToken);

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.URLENC)
                .cookie("JSESSIONID", initialSession)
                .formParams(formParams)
                .log().all()
                .when()
                .post(LOGIN_PATH)
                .then()
                .log().all()
                .extract()
                .response();

        // Store session cookie if login successful (302 to dashboard means success)
        String newSession = response.getCookie("JSESSIONID");
        if (newSession != null) {
            sessionCookie = newSession;
        } else {
            sessionCookie = initialSession;
        }

        // Check if redirected to dashboard (success) or back to login (failure)
        String location = response.getHeader("Location");
        if (location != null && !location.contains("login")) {
            log.info("Login successful, session: {}", sessionCookie);
        } else {
            log.warn("Login may have failed, redirected to: {}", location);
        }

        return response;
    }

    private String extractCsrfToken(String html) {
        // Pattern to match CSRF token in hidden input field
        Pattern pattern = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Try alternate pattern
        pattern = Pattern.compile("value=\"([^\"]+)\"\\s+name=\"_csrf\"");
        matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        log.warn("Could not find CSRF token in page");
        return "";
    }

    public Response logout() {
        log.info("Logging out");

        Response response = RestAssured.given()
                .baseUri(BASE_URL)
                .contentType(ContentType.URLENC)
                .cookie("JSESSIONID", sessionCookie)
                .log().all()
                .when()
                .post(LOGOUT_PATH)
                .then()
                .log().all()
                .extract()
                .response();

        sessionCookie = null;
        return response;
    }

    public Response getLoginPage() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .log().all()
                .when()
                .get(LOGIN_PATH)
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response getDashboard() {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .cookie("JSESSIONID", sessionCookie)
                .log().all()
                .when()
                .get(DASHBOARD_PATH)
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response accessProtectedResource(String path) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .cookie("JSESSIONID", sessionCookie)
                .log().all()
                .when()
                .get(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    public Response accessWithoutAuth(String path) {
        return RestAssured.given()
                .baseUri(BASE_URL)
                .log().all()
                .when()
                .get(path)
                .then()
                .log().all()
                .extract()
                .response();
    }

    public boolean isLoggedIn() {
        return sessionCookie != null && !sessionCookie.isEmpty();
    }

    public String getSessionCookie() {
        return sessionCookie;
    }

    public void setSessionCookie(String sessionCookie) {
        this.sessionCookie = sessionCookie;
    }

    public void clearSession() {
        this.sessionCookie = null;
    }
}