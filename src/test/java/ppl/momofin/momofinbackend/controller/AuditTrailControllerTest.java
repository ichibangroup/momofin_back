package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailControllerTest {

    private AuditTrailController auditTrailController;

    @Mock
    private AuditTrailService auditTrailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditTrailController = new AuditTrailController(auditTrailService);
    }

    @Test
    void getAllAudits_shouldReturnPagedResponse() {
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
        when(auditTrailService.getAuditTrails(username, action, startDateTime, endDateTime, documentName, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                username, action, startDate, endDate, documentName, page, size, sortBy, direction
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        verify(auditTrailService, times(1))
                .getAuditTrails(username, action, startDateTime, endDateTime, documentName, pageable);
    }

    @Test
    void getAllAudits_shouldHandleDefaultParameters() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                null, null, null, null, null, 0, 10, "timestamp", "DESC"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(auditTrailService, times(1)).getAuditTrails(null, null, null, null, null, pageable);
    }

    @Test
    void getAllAudits_shouldHandleInvalidDateFormat() {
        String invalidStartDate = "invalidDate";

        assertThrows(Exception.class, () -> {
            auditTrailController.getAllAudits(
                    null, null, invalidStartDate, null, null, 0, 10, "timestamp", "DESC"
            );
        });
    }

    @Test
    void getAllAudits_shouldResolveSortFieldUsernameCorrectly() {
        String sortBy = "username";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "user.username"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                null, null, null, null, null, 0, 10, sortBy, "DESC"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(auditTrailService, times(1)).getAuditTrails(null, null, null, null, null, pageable);
    }

    @Test
    void getAllAudits_shouldResolveSortFieldDocumentNameCorrectly() {
        String sortBy = "documentName";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "document.name"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                null, null, null, null, null, 0, 10, sortBy, "DESC"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(auditTrailService, times(1)).getAuditTrails(null, null, null, null, null, pageable);
    }

    @Test
    void getAllAudits_shouldResolveSortFieldActionCorrectly() {
        String sortBy = "action";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "action"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                null, null, null, null, null, 0, 10, sortBy, "DESC"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(auditTrailService, times(1)).getAuditTrails(null, null, null, null, null, pageable);
    }

    @Test
    void getAllAudits_shouldDefaultToTimestampSortWhenInvalidSortFieldProvided() {
        String invalidSortField = "invalidField";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditTrail> auditTrailPage = new PageImpl<>(List.of());
        when(auditTrailService.getAuditTrails(null, null, null, null, null, pageable))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(
                null, null, null, null, null, 0, 10, invalidSortField, "DESC"
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());

        verify(auditTrailService, times(1)).getAuditTrails(null, null, null, null, null, pageable);
    }
}