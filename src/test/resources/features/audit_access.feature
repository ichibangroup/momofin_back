Feature: Access the audits endpoint

  Scenario: Successful access to the audits endpoint with a valid JWT token
    Given the user logs in with organization "Momofin", username "greatwhiteshark", and password "insecurepassword123"
    When the user retrieves a JWT token
    And the user accesses the "http://localhost:8080/audit/audits" endpoint
    Then the response status should be 200
    And the response time should be under 2 seconds
