package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ppl.momofin.momofinbackend.dto.EditRequestDTO;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.EditRequestKey;

import java.util.List;
import java.util.UUID;

public interface EditRequestRepository extends JpaRepository<EditRequest, EditRequestKey> {
    @Query("SELECT new ppl.momofin.momofinbackend.dto.EditRequestDTO("
            + "e.document.documentId, "
            + "e.user.userId, "
            + "e.document.owner.organization.name, "
            + "e.document.owner.username, "
            + "e.document.owner.position, "
            + "e.document.owner.email, "
            + "e.document.name) "
            + "FROM EditRequest e "
            + "WHERE e.user.userId = :userId")
    List<EditRequestDTO> findByUserIdAsDTO(@Param("userId") UUID userId);
}
