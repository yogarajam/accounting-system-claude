package com.accounting.api.steps;

import com.accounting.api.client.AuthApiClient;
import com.accounting.api.client.BaseApiClient;
import com.accounting.api.config.TestConfig;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common Step Definitions
 * Shared steps across all feature files
 */
@Slf4j
public class CommonSteps {

    protected static Response lastResponse;
    protected static AuthApiClient authClient = new AuthApiClient();

    @Before
    public void setup() {
        RestAssured.baseURI = TestConfig.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Given("the API is available")
    public void theApiIsAvailable() {
        Response response = SerenityRest.given()
                .baseUri(TestConfig.getBaseUrl())
                .when()
                .get("/login")
                .then()
                .extract()
                .response();

        assertThat(response.getStatusCode())
                .as("API should be available")
                .isIn(200, 302);

        log.info("API is available at: {}", TestConfig.getBaseUrl());
    }

    @Given("I am authenticated as admin")
    public void iAmAuthenticatedAsAdmin() {
        authClient.login(TestConfig.getUsername(), TestConfig.getPassword());
        assertThat(authClient.isLoggedIn())
                .as("Should be logged in")
                .isTrue();
        // Share session cookie with all API clients
        BaseApiClient.setSharedSessionCookie(authClient.getSessionCookie());
        log.info("Authenticated as admin, session shared");
    }

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        authClient.clearSession();
        assertThat(authClient.isLoggedIn())
                .as("Should not be logged in")
                .isFalse();
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatusCode) {
        assertThat(lastResponse.getStatusCode())
                .as("Response status code should be %d", expectedStatusCode)
                .isEqualTo(expectedStatusCode);
    }

    @Then("the response should contain {string}")
    public void theResponseShouldContain(String expectedContent) {
        assertThat(lastResponse.getBody().asString())
                .as("Response should contain: %s", expectedContent)
                .contains(expectedContent);
    }

    @Then("the response should not be empty")
    public void theResponseShouldNotBeEmpty() {
        assertThat(lastResponse.getBody().asString())
                .as("Response should not be empty")
                .isNotEmpty();
    }

    public static void setLastResponse(Response response) {
        lastResponse = response;
    }

    public static Response getLastResponse() {
        return lastResponse;
    }

    public static AuthApiClient getAuthClient() {
        return authClient;
    }
}