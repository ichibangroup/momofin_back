package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ppl.momofin.momofinbackend.model.AuditTrail;
import ppl.momofin.momofinbackend.repository.AuditTrailRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
    void whenGetAuditTrails_thenShouldReturnPagedResultsWithSorting() {
        // Arrange
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        AuditTrail trail2 = new AuditTrail();
        trail2.setAction("VERIFY");

        List<AuditTrail> auditTrails = Arrays.asList(trail1, trail2);
        when(auditTrailRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(auditTrails));

        Page<AuditTrail> result = auditTrailService.getAuditTrails(0, 2, "action", "ASC");

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("SUBMIT");
        assertThat(result.getContent().get(1).getAction()).isEqualTo("VERIFY");

        verify(auditTrailRepository, times(1)).findAll(any(PageRequest.class));
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
