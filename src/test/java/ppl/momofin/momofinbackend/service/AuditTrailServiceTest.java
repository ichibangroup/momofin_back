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
    void whenGetAuditTrailsWithoutFilters_thenShouldReturnAllResults() {
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        AuditTrail trail2 = new AuditTrail();
        trail2.setAction("VERIFY");

        List<AuditTrail> auditTrails = Arrays.asList(trail1, trail2);
        Page<AuditTrail> page = new PageImpl<>(auditTrails, PageRequest.of(0, 10), auditTrails.size());

        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(null, null, 0, 10, "timestamp", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getAction()).isEqualTo("SUBMIT");
        assertThat(result.getContent().get(1).getAction()).isEqualTo("VERIFY");

        verify(auditTrailRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenGetAuditTrailsWithActionFilter_thenShouldReturnFilteredResults() {
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        AuditTrail trail2 = new AuditTrail();
        trail2.setAction("VERIFY");

        List<AuditTrail> auditTrails = List.of(trail1);
        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(auditTrails));

        Page<AuditTrail> result = auditTrailService.getAuditTrails("SUBMIT", null, 0, 10, "timestamp", "ASC");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getAction()).isEqualTo("SUBMIT");

        verify(auditTrailRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenGetAuditTrailsWithUserFilter_thenShouldReturnFilteredResults() {
        User dummy = new User();
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        trail1.setUser(dummy);

        List<AuditTrail> auditTrails = List.of(trail1);
        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(auditTrails));

        Page<AuditTrail> result = auditTrailService.getAuditTrails(null, "user1", 0, 10, "timestamp", "ASC");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getUser()).isEqualTo(dummy);

        verify(auditTrailRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenGetAuditTrailsWithSorting_thenShouldReturnSortedResults() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 10, 30, 45);
        LocalDateTime anotherDateTime = LocalDateTime.of(2024, 1, 1, 10, 30, 45);
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        trail1.setTimestamp(dateTime);

        AuditTrail trail2 = new AuditTrail();
        trail2.setAction("VERIFY");
        trail2.setTimestamp(anotherDateTime);

        List<AuditTrail> auditTrails = Arrays.asList(trail2, trail1);
        when(auditTrailRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(auditTrails));

        Page<AuditTrail> result = auditTrailService.getAuditTrails(null, null, 0, 10, "timestamp", "ASC");

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTimestamp()).isEqualTo(anotherDateTime);
        assertThat(result.getContent().get(1).getTimestamp()).isEqualTo(dateTime);

        verify(auditTrailRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void whenGetAuditTrailsWithPagination_thenShouldReturnPagedResults() {
        AuditTrail trail1 = new AuditTrail();
        trail1.setAction("SUBMIT");
        AuditTrail trail2 = new AuditTrail();
        trail2.setAction("VERIFY");

        List<AuditTrail> auditTrails = Arrays.asList(trail1);
        Page<AuditTrail> page = new PageImpl<>(auditTrails, PageRequest.of(0, 1), 2);

        when(auditTrailRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<AuditTrail> result = auditTrailService.getAuditTrails(null, null, 0, 1, "timestamp", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().getFirst().getAction()).isEqualTo("SUBMIT");

        verify(auditTrailRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
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
