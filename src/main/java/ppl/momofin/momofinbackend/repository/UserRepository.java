package ppl.momofin.momofinbackend.repository;

import ppl.momofin.momofinbackend.model.Organization;
import ppl.momofin.momofinbackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrganization(Organization organization);
    Optional<User> findUserByOrganizationAndEmailAndPassword(Organization organization, String email, String password);

    @Query("SELECT u FROM User u WHERE (u.organization = :organization AND u.name = :name) OR u.email = :email")
    List<User> findByOrganizationAndNameOrEmail(@Param("organization") Organization organization,
                                                    @Param("name") String name,
                                                    @Param("email") String email);
}
