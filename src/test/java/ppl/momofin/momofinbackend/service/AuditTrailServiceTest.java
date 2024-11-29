package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailServiceImplTest {

    private AuditTrailServiceImpl auditTrailService;

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        auditTrailService = new AuditTrailServiceImpl(auditTrailRepository);
    }

    @Test
    void getAuditTrails_shouldReturnPagedAuditTrails() {
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
                username, action, startDateTime, endDateTime, documentName, pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(action, result.getContent().get(0).getAction());

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
    }

    @Test
    void getAuditTrails_shouldHandleNullParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(
                null, null, null, null, null, pageable
        );

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
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
        String username = "testUser";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(username, null, null, null, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
    }

    @Test
    void getAuditTrails_shouldFilterByActionOnly() {
        String action = "SUBMIT";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(null, action, null, null, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
    }

    @Test
    void getAuditTrails_shouldFilterByDateRange() {
        LocalDateTime startDateTime = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 1, 31, 23, 59);
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditTrail> mockPage = new PageImpl<>(List.of());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        auditTrailService.getAuditTrails(null, null, startDateTime, endDateTime, null, pageable);

        ArgumentCaptor<Specification<AuditTrail>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(auditTrailRepository, times(1)).findAll(captor.capture(), eq(pageable));
        Specification<AuditTrail> capturedSpec = captor.getValue();

        assertNotNull(capturedSpec);
    }
}