package ppl.momofin.momofinbackend.repository;

import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    List<User> findAllByOrganization(Organization organization);
    Optional<User> findUserByOrganizationAndUsername(Organization organization, String username);
    Optional<User> findByUsername(String username);
    List<User> findByOrganization(Organization organization);
}
