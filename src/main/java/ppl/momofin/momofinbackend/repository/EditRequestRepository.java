package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ppl.momofin.momofinbackend.model.EditRequest;
import ppl.momofin.momofinbackend.model.EditRequestKey;

import java.util.List;
import java.util.UUID;

public interface EditRequestRepository extends JpaRepository<EditRequest, EditRequestKey> {
    @Query("SELECT e FROM EditRequest e WHERE e.id.userId = :userId")
    List<EditRequest> findByUserId(@Param("userId") UUID userId);
}
