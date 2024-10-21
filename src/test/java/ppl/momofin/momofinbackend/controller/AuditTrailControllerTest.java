package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuditTrailControllerTest {

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private AuditTrailController auditTrailController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAudits_Success() {
        List<AuditTrail> mockAuditTrails = getAuditTrails();

        List<AuditTrailResponse> expectedResponses = mockAuditTrails.stream()
                .map(AuditTrailResponse::fromAuditTrail)
                .toList();

        when(auditTrailService.getAllAuditTrails()).thenReturn(mockAuditTrails);

        ResponseEntity<List<AuditTrailResponse>> response = auditTrailController.getAllAudits();

        List<AuditTrailResponse> actualResponses = response.getBody();

        for (int i = 0; i < expectedResponses.size(); i++) {
            AuditTrailResponse expected = expectedResponses.get(i);
            AuditTrailResponse actual = actualResponses.get(i);
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getUsername(), actual.getUsername());
            assertEquals(expected.getDocument(), actual.getDocument());
            assertEquals(expected.getAction(), actual.getAction());
            assertEquals(expected.getOutcome(), actual.getOutcome());
        }
    }

    private static List<AuditTrail> getAuditTrails() {
        User testuser = new User();
        testuser.setUserId(1L);
        testuser.setUsername("tester");

        Document testdoc = new Document();
        testdoc.setDocumentId(1L);
        testdoc.setName("testdoc");

        AuditTrail audit1 = new AuditTrail();
        audit1.setId(1L);
        audit1.setUser(testuser);
        audit1.setDocument(testdoc);

        AuditTrail audit2 = new AuditTrail();
        audit2.setId(2L);
        audit2.setUser(testuser);
        audit2.setDocument(testdoc);

        return Arrays.asList(audit1, audit2);
    }


    @Test
    void testGetAuditTrailById() {
        AuditTrail mockAuditTrail = new AuditTrail();
        mockAuditTrail.setId(1L);
        when(auditTrailService.getAuditTrailById(1L)).thenReturn(mockAuditTrail);

        ResponseEntity<AuditTrail> response = auditTrailController.getAuditTrailById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAuditTrail, response.getBody());
        verify(auditTrailService, times(1)).getAuditTrailById(1L);
    }

    @Test
    void testCreateAuditTrail() {
        AuditTrail mockAuditTrail = new AuditTrail();
        mockAuditTrail.setId(1L);
        when(auditTrailService.createAuditTrail(any(AuditTrail.class))).thenReturn(mockAuditTrail);

        ResponseEntity<AuditTrail> response = auditTrailController.createAuditTrail(mockAuditTrail);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockAuditTrail, response.getBody());
        verify(auditTrailService, times(1)).createAuditTrail(mockAuditTrail);
    }

    @Test
    void testDeleteAuditTrail() {
        ResponseEntity<Void> response = auditTrailController.deleteAuditTrail(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(auditTrailService, times(1)).deleteAuditTrail(1L);
    }
}
