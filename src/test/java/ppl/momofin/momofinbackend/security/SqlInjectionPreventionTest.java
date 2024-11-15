package ppl.momofin.momofinbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ppl.momofin.momofinbackend.error.SecurityValidationException;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.repository.UserRepository;
import ppl.momofin.momofinbackend.service.OrganizationServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqlInjectionPreventionTest {
    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SqlInjectionValidator sqlInjectionValidator;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private UUID orgId;
    private Organization testOrg;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();
        testOrg = new Organization("Test Org", "Test Description", "Test Industry", "Test Location");
        testOrg.setOrganizationId(orgId);
    }

    @Test
    void createOrganization_WithSqlInjection_ShouldThrowException() {
        // Arrange
        String maliciousName = "company'; DROP TABLE users;--";

        // Only stub the first check since it will throw immediately
        when(sqlInjectionValidator.containsSqlInjection(maliciousName)).thenReturn(true);

        // Act & Assert
        SecurityValidationException exception = assertThrows(SecurityValidationException.class, () ->
                organizationService.createOrganization(maliciousName, "description", "Tech", "NY")
        );
        assertEquals("SQL injection detected in input", exception.getMessage());
    }

    @Test
    void createOrganization_WithValidInput_ShouldSucceed() {
        // Arrange
        String validName = "Tech Company Inc";
        String validDescription = "A good company";
        Organization expectedOrg = new Organization(validName, validDescription, "Tech", "NY");

        when(sqlInjectionValidator.containsSqlInjection(any())).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenReturn(expectedOrg);

        // Act
        Organization result = organizationService.createOrganization(validName, validDescription, "Tech", "NY");

        // Assert
        assertNotNull(result);
        assertEquals(validName, result.getName());
    }

    @Test
    void updateOrganization_WithSqlInjection_ShouldThrowException() {
        // Arrange
        String maliciousName = "company'; TRUNCATE TABLE users;--";

        // Only stub the first check since it will throw immediately
        when(sqlInjectionValidator.containsSqlInjection(maliciousName)).thenReturn(true);

        // Act & Assert
        SecurityValidationException exception = assertThrows(SecurityValidationException.class, () ->
                organizationService.updateOrganization(orgId, maliciousName, "description", "Tech", "NY")
        );
        assertEquals("SQL injection detected in input", exception.getMessage());
    }

    @Test
    void updateOrganization_WithValidInput_ShouldSucceed() {
        // Arrange
        String validName = "Updated Tech Corp";
        Organization updatedOrg = new Organization(validName, "New Desc", "Tech", "NY");
        updatedOrg.setOrganizationId(orgId);

        when(sqlInjectionValidator.containsSqlInjection(any())).thenReturn(false);
        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(testOrg));
        when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

        // Act
        Organization result = organizationService.updateOrganization(orgId, validName, "New Desc", "Tech", "NY");

        // Assert
        assertNotNull(result);
        assertEquals(validName, result.getName());
    }
}