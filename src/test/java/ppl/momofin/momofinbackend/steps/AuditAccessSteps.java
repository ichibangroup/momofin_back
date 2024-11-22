package ppl.momofin.momofinbackend.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.lessThan;
@SpringBootTest
public class AuditAccessSteps {

    private String jwtToken;
    private Response response;

    @Given("the user logs in with organization {string}, username {string}, and password {string}")
    public void theUserLogsIn(String organizationName, String username, String password) {
        // Set the login URL for the authentication endpoint
        String loginUrl = "http://localhost:8080/auth/login";

        // Build the login request with JSON content type and credentials in the body
        RequestSpecification request = RestAssured.given()
                .contentType("application/json")
                .body("{\"organizationName\": \"" + organizationName + "\", " +
                        "\"username\": \"" + username + "\", " +
                        "\"password\": \"" + password + "\"}");

        // Send POST request to authenticate and capture the response
        response = request.post(loginUrl);

        // Verify that the login request was successful
        assertEquals("Failed to login, expected HTTP status 200", 200, response.statusCode());

        // Retrieve the JWT token from the response if login is successful
        jwtToken = response.jsonPath().getString("jwt");

        // Verify that the token was retrieved
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new RuntimeException("JWT token not retrieved from login response");
        }
        System.out.println("Retrieved JWT Token: " + jwtToken);
    }

    @When("the user accesses the {string} endpoint")
    public void theUserAccessesEndpoint(String endpoint) {
        // Ensure the JWT token has been set before making an authenticated request
        if (jwtToken == null || jwtToken.isEmpty()) {
            throw new IllegalStateException("JWT token is missing. Ensure that login was successful before accessing secured endpoints.");
        }

        // Create a request with the Authorization header using the retrieved JWT token
        RequestSpecification request = RestAssured.given()
                .header("Authorization", "Bearer " + jwtToken);

        // Send GET request to the specified endpoint
        response = request.get(endpoint);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        // Verify that the response status matches the expected status code
        response.then().statusCode(statusCode);
    }

    @Then("the response time should be under {int} seconds")
    public void theResponseTimeShouldBeUnderSeconds(int seconds) {
        // Verify that the response time is less than the specified limit in milliseconds
        response.then().time(lessThan((long) seconds * 1000));
    }
}
