package ppl.momofin.momofinbackend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.response.AuditTrailResponse;
import ppl.momofin.momofinbackend.service.AuditTrailService;
import ppl.momofin.momofinbackend.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuditTrailControllerTest {

    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuditTrailController auditTrailController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAudits_withFilters() {
        String username = "testUser";
        String action = "SUBMIT";
        String startDate = "2023-10-01T08:00:00";
        String endDate = "2023-10-02T13:00:00";
        int page = 0;
        int size = 10;
        String sortBy = "timestamp";
        String direction = "DESC";

        User mockUser = new User();
        mockUser.setUsername(username);

        Document mockDoc = new Document();
        mockDoc.setName("dummydoc");

        when(userService.fetchUserByUsername(username)).thenReturn(mockUser);

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(mockUser);
        auditTrail.setDocument(mockDoc);
        auditTrail.setAction(action);
        auditTrail.setTimestamp(LocalDateTime.parse(startDate));

        List<AuditTrail> auditTrailList = Collections.singletonList(auditTrail);
        Page<AuditTrail> auditTrailPage = new PageImpl<>(auditTrailList, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy)), auditTrailList.size());

        when(auditTrailService.getAuditTrails(eq(mockUser), eq(action), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(username, action, startDate, endDate, page, size, sortBy, direction);

        assertEquals(1, Objects.requireNonNull(response.getBody()).getContent().size());

        AuditTrailResponse auditTrailResponse = response.getBody().getContent().getFirst();
        assertEquals(auditTrail.getId(), auditTrailResponse.getId());
        assertEquals(auditTrail.getUser().getUsername(), auditTrailResponse.getUsername());
        assertEquals(auditTrail.getAction(), auditTrailResponse.getAction());
        assertEquals(auditTrail.getTimestamp().format(DateTimeFormatter.ofPattern("H:mm • d MMM, uuuu")), auditTrailResponse.getDate());
    }

    @Test
    void testGetAllAudits_withoutFilters() {
        int page = 0;
        int size = 10;
        String sortBy = "timestamp";
        String direction = "DESC";

        User mockUser = new User();
        mockUser.setUsername("dummyuser");
    private static List<AuditTrail> getAuditTrails() {
        User testuser = new User();
        testuser.setUserId(UUID.fromString("292aeace-0148-4a20-98bf-bf7f12871efe"));
        testuser.setUsername("tester");

        Document mockDoc = new Document();
        mockDoc.setName("dummydoc");
        Document testdoc = new Document();
        testdoc.setDocumentId(UUID.fromString("bd7ef7cf-8875-45fb-9fe5-f36319acddff"));
        testdoc.setName("testdoc");

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(mockUser);
        auditTrail.setDocument(mockDoc);
        auditTrail.setAction("VERIFY");
        auditTrail.setTimestamp(LocalDateTime.now());

        List<AuditTrail> auditTrailList = Collections.singletonList(auditTrail);
        Page<AuditTrail> auditTrailPage = new PageImpl<>(auditTrailList, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy)), auditTrailList.size());

        when(auditTrailService.getAuditTrails(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(auditTrailPage);

        ResponseEntity<Page<AuditTrailResponse>> response = auditTrailController.getAllAudits(null, null, null, null, page, size, sortBy, direction);

        assertEquals(1, Objects.requireNonNull(response.getBody()).getContent().size());

        AuditTrailResponse auditTrailResponse = response.getBody().getContent().getFirst();
        assertEquals(auditTrail.getId(), auditTrailResponse.getId());
        assertEquals(auditTrail.getAction(), auditTrailResponse.getAction());
        assertEquals(auditTrail.getTimestamp().format(DateTimeFormatter.ofPattern("H:mm • d MMM, uuuu")), auditTrailResponse.getDate());
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