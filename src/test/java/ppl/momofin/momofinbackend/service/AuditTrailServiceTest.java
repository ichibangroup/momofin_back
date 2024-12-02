package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailServiceTest {

    private AuditTrailServiceImpl auditTrailService;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditTrailService = new AuditTrailServiceImpl(auditTrailRepository);
    }

    @Test
    void getAuditTrails_shouldReturnPagedAuditTrails() {
        // Mock organization
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        // Mock JWT token and extraction
        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        // Mock organization repository
        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        // Define test parameters
        String username = "testUser";
        String action = "SUBMIT";
        String documentName = "testDocument";
        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));

        // Mock audit trail repository behavior
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setAction(action);
        auditTrail.setTimestamp(startDateTime);

        Page<AuditTrail> mockPage = new PageImpl<>(List.of(auditTrail), pageable, 1);
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        Page<AuditTrail> result = auditTrailService.getAuditTrails(
                org, username, action, startDateTime, endDateTime, documentName, pageable
        );

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getTotalElements(), "Total elements should match the mock data");
        assertEquals(action, result.getContent().get(0).getAction(), "Action should match the mock data");

        // Verify repository interaction
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec, "Captured specification should not be null");
    }


    @Test
    void getAuditTrails_shouldHandleNullParameters() {
        // Mock organization
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        // Mock pageable and repository behavior
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        Page<AuditTrail> result = auditTrailService.getAuditTrails(
                mockOrganization, null, null, null, null, null, pageable
        );

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0 for an empty page");

        // Capture and verify the specification passed to the repository
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec, "Captured specification should not be null");
    }


    @Test
    void createAuditTrail_shouldSaveAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setAction("SUBMIT");

        auditTrailService.createAuditTrail(auditTrail);

        ArgumentCaptor<AuditTrail> captor = ArgumentCaptor.forClass(AuditTrail.class);
        verify(auditTrailRepository, times(1)).save(captor.capture());

        AuditTrail capturedAuditTrail = captor.getValue();
        assertEquals("SUBMIT", capturedAuditTrail.getAction());
    }

    @Test
    void getAuditTrails_shouldFilterByUsernameOnly() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        // Mock JWT token and extraction
        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        // Mock organization repository
        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        String username = "testUser";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(org, username, null, null, null, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
    }

    @Test
    void getAuditTrails_shouldFilterByActionOnly() {
        // Mock organization
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        // Mock action filter
        String action = "SUBMIT";

        // Mock pageable and repository response
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        auditTrailService.getAuditTrails(mockOrganization, null, action, null, null, null, pageable);

        // Capture and verify the specification passed to the repository
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        // Assert
        assertNotNull(capturedSpec, "Captured specification should not be null");
    }

    @Test
    void getAuditTrails_shouldFilterByDateRange() {
        // Mock organization
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        // Mock date range and pageable
        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        // Mock repository response
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        auditTrailService.getAuditTrails(mockOrganization, null, null, startDateTime, endDateTime, null, pageable);

        // Capture and verify the specification passed to the repository
        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        // Assert
        assertNotNull(capturedSpec, "Captured specification should not be null");
    }
}