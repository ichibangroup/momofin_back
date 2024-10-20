package ppl.momofin.momofinbackend.repository;

import ppl.momofin.momofinbackend.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    Optional<Organization> findOrganizationByName(String name);
}
