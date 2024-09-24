package repository;

import model.Organization;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrganization(Organization organization);
    Optional<User> findUserByOrganizationAndNameAndPassword(Organization organization, String name, String password);

    @Query("SELECT u FROM User u WHERE (u.organization = :organization AND u.name = :name) OR u.email = :email")
    List<User> findByOrganizationAndNameOrEmail(@Param("organization") Organization organization,
                                                    @Param("name") String name,
                                                    @Param("email") String email);
}
