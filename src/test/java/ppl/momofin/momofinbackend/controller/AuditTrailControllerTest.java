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

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuditTrailControllerTest {

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private AuditTrailController auditTrailController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes @Mock and @InjectMocks annotations
    }

    // Test for getAllAuditTrails()
    @Test
    public void testGetAllAuditTrails() {
        // Arrange
        AuditTrail auditTrail1 = new AuditTrail();
        auditTrail1.setId(1L);

        AuditTrail auditTrail2 = new AuditTrail();
        auditTrail2.setId(2L);

        List<AuditTrail> mockAuditTrails = Arrays.asList(auditTrail1, auditTrail2);
        when(auditTrailService.getAllAuditTrails()).thenReturn(mockAuditTrails);

        // Act
        ResponseEntity<List<AuditTrail>> response = auditTrailController.getAllAuditTrails();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAuditTrails, response.getBody());
        verify(auditTrailService, times(1)).getAllAuditTrails();
    }

    // Test for getAuditTrailById()
    @Test
    public void testGetAuditTrailById() {
        // Arrange
        AuditTrail mockAuditTrail = new AuditTrail();
        mockAuditTrail.setId(1L);
        when(auditTrailService.getAuditTrailById(1L)).thenReturn(mockAuditTrail);

        // Act
        ResponseEntity<AuditTrail> response = auditTrailController.getAuditTrailById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAuditTrail, response.getBody());
        verify(auditTrailService, times(1)).getAuditTrailById(1L);
    }

    // Test for createAuditTrail()
    @Test
    public void testCreateAuditTrail() {
        // Arrange
        AuditTrail mockAuditTrail = new AuditTrail();
        mockAuditTrail.setId(1L);
        when(auditTrailService.createAuditTrail(any(AuditTrail.class))).thenReturn(mockAuditTrail);

        // Act
        ResponseEntity<AuditTrail> response = auditTrailController.createAuditTrail(mockAuditTrail);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockAuditTrail, response.getBody());
        verify(auditTrailService, times(1)).createAuditTrail(mockAuditTrail);
    }

    // Test for deleteAuditTrail()
    @Test
    public void testDeleteAuditTrail() {
        // Act
        ResponseEntity<Void> response = auditTrailController.deleteAuditTrail(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(auditTrailService, times(1)).deleteAuditTrail(1L);
    }
}
