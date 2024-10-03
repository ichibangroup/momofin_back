package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuditTrailControllerTest {

    @InjectMocks
    private AuditTrailController auditTrailController;

    @Mock
    private AuditTrailService auditTrailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAuditTrails() {
        List<AuditTrail> auditTrails = new ArrayList<>();
        when(auditTrailService.getAllAuditTrails()).thenReturn(auditTrails);

        ResponseEntity<List<AuditTrail>> response = auditTrailController.getAllAuditTrails();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(auditTrails, response.getBody());
        verify(auditTrailService, times(1)).getAllAuditTrails();
    }

    @Test
    void testGetAuditTrailById() {
        Long id = 1L;
        AuditTrail auditTrail = new AuditTrail();
        when(auditTrailService.getAuditTrailById(id)).thenReturn(auditTrail);

        ResponseEntity<AuditTrail> response = auditTrailController.getAuditTrailById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(auditTrail, response.getBody());
        verify(auditTrailService, times(1)).getAuditTrailById(id);
    }

    @Test
    void testCreateAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        when(auditTrailService.createAuditTrail(auditTrail)).thenReturn(auditTrail);

        ResponseEntity<AuditTrail> response = auditTrailController.createAuditTrail(auditTrail);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(auditTrail, response.getBody());
        verify(auditTrailService, times(1)).createAuditTrail(auditTrail);
    }

    @Test
    void testDeleteAuditTrail() {
        Long id = 1L;

        ResponseEntity<Void> response = auditTrailController.deleteAuditTrail(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(auditTrailService, times(1)).deleteAuditTrail(id);
    }
}
