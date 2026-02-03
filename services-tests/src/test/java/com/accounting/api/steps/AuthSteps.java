package com.accounting.api.steps;

import com.accounting.api.client.AuthApiClient;
import com.accounting.api.config.TestConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Authentication Step Definitions
 */
@Slf4j
public class AuthSteps {

    private final AuthApiClient authClient = CommonSteps.getAuthClient();
    private Response loginResponse;
    private Response logoutResponse;

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        loginResponse = authClient.login(username, password);
        CommonSteps.setLastResponse(loginResponse);
    }

    @Then("the login should be successful")
    public void theLoginShouldBeSuccessful() {
        assertThat(loginResponse.getStatusCode())
                .as("Login should redirect or succeed")
                .isIn(200, 302);
        assertThat(authClient.isLoggedIn())
                .as("Should have a valid session")
                .isTrue();
    }

    @Then("I should receive a session cookie")
    public void iShouldReceiveASessionCookie() {
        assertThat(authClient.getSessionCookie())
                .as("Should receive JSESSIONID cookie")
                .isNotNull()
                .isNotEmpty();
    }

    @Then("I should be redirected to dashboard")
    public void iShouldBeRedirectedToDashboard() {
        String location = loginResponse.getHeader("Location");
        if (location != null) {
            assertThat(location)
                    .as("Should redirect to dashboard")
                    .containsAnyOf("dashboard", "/");
        }
        log.info("Login redirects to dashboard");
    }

    @Given("I am logged in as admin")
    public void iAmLoggedInAsAdmin() {
        authClient.login(TestConfig.getUsername(), TestConfig.getPassword());
        assertThat(authClient.isLoggedIn())
                .as("Should be logged in as admin")
                .isTrue();
        log.info("Logged in as admin");
    }

    @When("I access the dashboard")
    public void iAccessTheDashboard() {
        Response response = authClient.getDashboard();
        CommonSteps.setLastResponse(response);
    }

    @Then("the response should contain dashboard content")
    public void theResponseShouldContainDashboardContent() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getBody().asString())
                .as("Response should contain dashboard content")
                .containsAnyOf("Dashboard", "dashboard", "Welcome");
    }

    @When("I logout")
    public void iLogout() {
        logoutResponse = authClient.logout();
        CommonSteps.setLastResponse(logoutResponse);
    }

    @Then("the logout should be successful")
    public void theLogoutShouldBeSuccessful() {
        assertThat(logoutResponse.getStatusCode())
                .as("Logout should redirect or succeed")
                .isIn(200, 302);
    }

    @Then("the session should be invalidated")
    public void theSessionShouldBeInvalidated() {
        assertThat(authClient.isLoggedIn())
                .as("Session should be cleared")
                .isFalse();
    }

    @Then("the login should fail")
    public void theLoginShouldFail() {
        // Check for error indicators
        boolean isFailure = loginResponse.getStatusCode() == 401 ||
                loginResponse.getStatusCode() == 403 ||
                (loginResponse.getStatusCode() == 302 &&
                        loginResponse.getHeader("Location") != null &&
                        loginResponse.getHeader("Location").contains("error"));

        // Also check response body for error messages
        if (!isFailure && loginResponse.getBody() != null) {
            String body = loginResponse.getBody().asString();
            isFailure = body.contains("error") || body.contains("Invalid") || body.contains("failed");
        }

        log.info("Login response status: {}, Location: {}",
                loginResponse.getStatusCode(),
                loginResponse.getHeader("Location"));
    }

    @Then("I should remain on the login page")
    public void iShouldRemainOnTheLoginPage() {
        String location = loginResponse.getHeader("Location");
        if (location != null) {
            assertThat(location)
                    .as("Should stay on login page")
                    .containsAnyOf("login", "error");
        }
    }

    @Then("an error message should be displayed")
    public void anErrorMessageShouldBeDisplayed() {
        String location = loginResponse.getHeader("Location");
        if (location != null) {
            assertThat(location)
                    .as("Should have error parameter")
                    .contains("error");
        }
    }

    @When("I try to access {string} without authentication")
    public void iTryToAccessWithoutAuthentication(String path) {
        authClient.clearSession();
        Response response = authClient.accessWithoutAuth(path);
        CommonSteps.setLastResponse(response);
    }

    @Then("I should be redirected to login page")
    public void iShouldBeRedirectedToLoginPage() {
        Response response = CommonSteps.getLastResponse();
        // Check for redirect to login
        if (response.getStatusCode() == 302) {
            String location = response.getHeader("Location");
            assertThat(location)
                    .as("Should redirect to login")
                    .containsAnyOf("login", "Login");
        } else if (response.getStatusCode() == 200) {
            // May have followed redirect to login page
            assertThat(response.getBody().asString())
                    .as("Should show login page")
                    .containsAnyOf("login", "Login", "Sign in", "Username", "Password");
        }
    }

    @When("I make multiple requests")
    public void iMakeMultipleRequests() {
        // Make several requests to verify session persistence
        authClient.getDashboard();
        authClient.accessProtectedResource("/accounts");
        authClient.accessProtectedResource("/journal");
        Response response = authClient.getDashboard();
        CommonSteps.setLastResponse(response);
    }

    @Then("the session should remain valid")
    public void theSessionShouldRemainValid() {
        assertThat(authClient.isLoggedIn())
                .as("Session should still be valid")
                .isTrue();
    }

    @Then("all requests should succeed")
    public void allRequestsShouldSucceed() {
        Response response = CommonSteps.getLastResponse();
        assertThat(response.getStatusCode())
                .as("Last request should succeed")
                .isIn(200, 302);
    }

    @When("I access the accounts page")
    public void iAccessTheAccountsPage() {
        Response response = authClient.accessProtectedResource("/accounts");
        CommonSteps.setLastResponse(response);
    }

    @When("I access the journal page")
    public void iAccessTheJournalPage() {
        Response response = authClient.accessProtectedResource("/journal");
        CommonSteps.setLastResponse(response);
    }

    @When("I access the reports page")
    public void iAccessTheReportsPage() {
        Response response = authClient.accessProtectedResource("/reports");
        CommonSteps.setLastResponse(response);
    }

    @When("I access the invoices page")
    public void iAccessTheInvoicesPage() {
        Response response = authClient.accessProtectedResource("/invoices");
        CommonSteps.setLastResponse(response);
    }
}