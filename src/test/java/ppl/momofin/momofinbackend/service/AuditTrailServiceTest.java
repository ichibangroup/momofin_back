package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.model.User;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditTrailServiceTest {

    @Mock
    private AuditTrailRepository auditTrailRepository;

    @InjectMocks
    private AuditTrailServiceImpl auditTrailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAuditTrails_withAllFilters() {
        User mockUser = new User();
        mockUser.setUsername("testUser");

        String action = "SUBMIT";
        LocalDateTime startDateTime = LocalDateTime.of(2023, 10, 1, 8, 0);
        LocalDateTime endDateTime = LocalDateTime.of(2023, 10, 2, 13, 0);
        Pageable pageable = PageRequest.of(0, 10);

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(1L);
        auditTrail.setUser(mockUser);
        auditTrail.setAction(action);
        auditTrail.setTimestamp(startDateTime);

        List<AuditTrail> auditTrailList = Collections.singletonList(auditTrail);
        Page<AuditTrail> auditTrailPage = new PageImpl<>(auditTrailList, pageable, auditTrailList.size());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(auditTrailPage);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(mockUser, action, startDateTime, endDateTime, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(auditTrail.getId(), result.getContent().getFirst().getId());
        verify(auditTrailRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetAuditTrails_withNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setId(2L);
        auditTrail.setAction("VERIFY");
        auditTrail.setTimestamp(LocalDateTime.now());

        List<AuditTrail> auditTrailList = Collections.singletonList(auditTrail);
        Page<AuditTrail> auditTrailPage = new PageImpl<>(auditTrailList, pageable, auditTrailList.size());

        when(auditTrailRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(auditTrailPage);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(auditTrail.getId(), result.getContent().getFirst().getId());
        verify(auditTrailRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testCreateAuditTrail() {
        AuditTrail auditTrail = new AuditTrail();
        when(auditTrailRepository.save(auditTrail)).thenReturn(auditTrail);

        AuditTrail created = auditTrailService.createAuditTrail(auditTrail);

        assertNotNull(created);
        verify(auditTrailRepository, times(1)).save(auditTrail);
    }

    @Test
    void testGetAllAuditTrails() {
        when(auditTrailRepository.findAll()).thenReturn(Collections.singletonList(new AuditTrail()));

        List<AuditTrail> auditTrails = auditTrailService.getAllAuditTrails();

        assertEquals(1, auditTrails.size());
        verify(auditTrailRepository, times(1)).findAll();
    }

    @Test
    void testGetAuditTrailById() {
        Long id = 1L;
        AuditTrail auditTrail = new AuditTrail();
        when(auditTrailRepository.findById(id)).thenReturn(Optional.of(auditTrail));

        AuditTrail found = auditTrailService.getAuditTrailById(id);

        assertNotNull(found);
        verify(auditTrailRepository, times(1)).findById(id);
    }

    @Test
    void testDeleteAuditTrail() {
        Long id = 1L;
        doNothing().when(auditTrailRepository).deleteById(id);

        auditTrailService.deleteAuditTrail(id);

        verify(auditTrailRepository, times(1)).deleteById(id);
    }
}
