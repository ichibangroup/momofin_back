package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        String username = "testUser";
        String action = "SUBMIT";
        String documentName = "testDocument";
        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setAction(action);
        auditTrail.setTimestamp(startDateTime);

        Page<AuditTrail> mockPage = new PageImpl<>(List.of(auditTrail), pageable, 1);
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(
                org, username, action, startDateTime, endDateTime, documentName, pageable
        );

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getTotalElements(), "Total elements should match the mock data");
        assertEquals(action, result.getContent().getFirst().getAction(), "Action should match the mock data");

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec, "Captured specification should not be null");
    }


    @Test
    void getAuditTrails_shouldHandleNullParameters() {
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(
                mockOrganization, null, null, null, null, null, pageable
        );

        assertNotNull(result, "Result should not be null");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0 for an empty page");

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

        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

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
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        String action = "SUBMIT";

        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(mockOrganization, null, action, null, null, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec, "Captured specification should not be null");
    }

    @Test
    void getAuditTrails_shouldFilterByDateRange() {
        Organization mockOrganization = new Organization();
        mockOrganization.setOrganizationId(UUID.randomUUID());
        mockOrganization.setName("Test Organization");

        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);

        Page<AuditTrail> mockPage = new PageImpl<>(List.of());
        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(mockOrganization, null, null, startDateTime, endDateTime, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec, "Captured specification should not be null");
    }
}