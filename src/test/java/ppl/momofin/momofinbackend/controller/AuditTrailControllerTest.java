package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.security.JwtUtil;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;


class AuditTrailControllerTest {

    private AuditTrailController auditTrailController;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditTrailController = new AuditTrailController(auditTrailService, organizationRepository, jwtUtil);
    }

    @Test
    void getAllAudits_shouldReturnPagedResponse() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        String username = "testUser";
        String action = "SUBMIT";
        String startDate = "2023-01-01T00:00:00";
        String endDate = "2023-01-31T23:59:59";
        String documentName = "testDoc";
        int page = 0;
        int size = 10;
        String sortBy = "timestamp";
        String direction = "DESC";

        LocalDateTime startDateTime = LocalDateTime.parse(startDate);
        LocalDateTime endDateTime = LocalDateTime.parse(endDate);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        AuditTrail mockAuditTrail = new AuditTrail();
        mockAuditTrail.setAction(action);
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of(mockAuditTrail), pageable, 1);

        when(auditTrailService.getAuditTrails(org, username, action, startDateTime, endDateTime, documentName, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, username, action, startDate, endDate, documentName, page, size, sortBy, direction
        );

        assertNotNull(response);
        assertEquals(1, Objects.requireNonNull(response.getBody()).getTotalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(jwtUtil, times(1)).extractOrganizationId("validToken");
        verify(organizationRepository, times(1)).findOrganizationByOrganizationId(UUID.fromString(orgId));
        verify(auditTrailService, times(1))
                .getAuditTrails(org, username, action, startDateTime, endDateTime, documentName, pageable);
    }


    @Test
    void getAllAudits_shouldHandleDefaultParameters() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of(), pageable, 0);
        when(auditTrailService.getAuditTrails(org, null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, null, null, null, null, null, 0, 10, "timestamp", "DESC"
        );

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(0, response.getBody().getTotalElements(), "Total elements should be 0 for an empty page");

        verify(jwtUtil, times(1)).extractOrganizationId("validToken");
        verify(organizationRepository, times(1)).findOrganizationByOrganizationId(UUID.fromString(orgId));
        verify(auditTrailService, times(1)).getAuditTrails(org, null, null, null, null, null, pageable);
    }


    @Test
    void getAllAudits_shouldHandleInvalidDateFormat() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        String invalidStartDate = "invalidDate";

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, null, null, invalidStartDate, null, null, 0, 10, "timestamp", "DESC"
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getAllAudits_shouldDefaultToTimestampSortWhenInvalidSortFieldProvided() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        when(auditTrailService.getAuditTrails(org, null, null, null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, null, null, null, null, null, 0, 10, "invalidField", "DESC"
        );

        System.out.println("Response Body: " + response.getBody());

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(0, response.getBody().getTotalElements(), "Total elements should be 0 for an empty page");

        verify(jwtUtil, times(1)).extractOrganizationId("validToken");
        verify(organizationRepository, times(1)).findOrganizationByOrganizationId(UUID.fromString(orgId));
        verify(auditTrailService, times(1)).getAuditTrails(org, null, null, null, null, null, pageable);
    }


    @Test
    void getAllAudits_shouldHandleExceptionAndReturnInternalServerError() {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        String username = "testUser";
        String action = "SUBMIT";
        String startDate = "2023-01-01T00:00:00";
        String endDate = "2023-01-31T23:59:59";
        String documentName = "testDoc";
        int page = 0;
        int size = 10;
        String sortBy = "timestamp";
        String direction = "DESC";

        when(auditTrailService.getAuditTrails(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, username, action, startDate, endDate, documentName, page, size, sortBy, direction
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(auditTrailService, times(1))
                .getAuditTrails(org, username, action, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate), documentName, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    @ParameterizedTest
    @MethodSource("sortFieldProvider")
    void getAllAudits_shouldResolveSortFieldsCorrectly(String sortBy, String expectedSortField) {
        Organization org = new Organization();
        org.setOrganizationId(UUID.randomUUID());
        org.setName("Test Organization");

        String token = "Bearer validToken";
        String orgId = org.getOrganizationId().toString();
        when(jwtUtil.extractOrganizationId("validToken")).thenReturn(orgId);

        when(organizationRepository.findOrganizationByOrganizationId(UUID.fromString(orgId)))
                .thenReturn(Optional.of(org));

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, expectedSortField));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(org, null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                token, null, null, null, null, null, 0, 10, sortBy, "DESC"
        );

        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals(0, response.getBody().getTotalElements(), "Total elements should be 0 for an empty page");

        verify(auditTrailService, times(1)).getAuditTrails(org, null, null, null, null, null, pageable);
    }


    private static Stream<Arguments> sortFieldProvider() {
        return Stream.of(
                Arguments.of("username", "user.username"),
                Arguments.of("documentName", "document.name"),
                Arguments.of("action", "action")
        );
    }
}