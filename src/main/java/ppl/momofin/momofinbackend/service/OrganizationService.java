package ppl.momofin.momofinbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.repository.OrganizationRepository;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Transactional
    public void populateNullDescriptions() {
        List<Organization> organizations = organizationRepository.findAll();
        for (Organization org : organizations) {
            if (org.getDescription() == null) {
                org.setDescription("Default organization description");
                organizationRepository.save(org);
            }
        }
    }

    // Other methods...
}