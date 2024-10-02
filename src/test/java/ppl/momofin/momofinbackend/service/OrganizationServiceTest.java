package ppl.momofin.momofinbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void populateNullDescriptions_ShouldUpdateOrganizationsWithNullDescriptions() {
        Organization org1 = new Organization("Org1");
        org1.setDescription(null);
        Organization org2 = new Organization("Org2", "Existing Description");

        when(organizationRepository.findAll()).thenReturn(Arrays.asList(org1, org2));

        organizationService.populateNullDescriptions();

        verify(organizationRepository).save(org1);
        verify(organizationRepository, never()).save(org2);
    }
}