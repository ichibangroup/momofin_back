package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.service.OrganizationService;
import ppl.momofin.momofinbackend.response.OrganizationResponse;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class MomofinAdminControllerTest {

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private MomofinAdminController momofinAdminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllOrganizations_shouldReturnListOfOrganizations() {
        // Arrange
        Organization org1 = new Organization("Org1", "Description1");
        Organization org2 = new Organization("Org2", "Description2");
        List<Organization> organizations = Arrays.asList(org1, org2);

        when(organizationService.getAllOrganizations()).thenReturn(organizations);

        // Act
        ResponseEntity<List<OrganizationResponse>> response = momofinAdminController.getAllOrganizations();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("Org1", response.getBody().get(0).getName());
        assertEquals("Org2", response.getBody().get(1).getName());
    }
}
