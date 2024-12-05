package ppl.momofin.momofinbackend.repository;

import ppl.momofin.momofinbackend.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findOrganizationByOrganizationId(UUID organizationId);
    Optional<Organization> findOrganizationByName(String name);
    Optional<Organization> findByName(String name);
}
