package ppl.momofin.momofinbackend.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByHashString(String hashString);
    Optional<Document> findByDocumentId(UUID documentId);
    List<Document> findAllByOwner(User user);

    @Transactional
    @Modifying
    @Query("UPDATE Document d SET d.isBeingRequested = :status WHERE d.documentId = :documentId")
    void updateIsBeingRequested(@Param("documentId") UUID documentId, @Param("status") boolean status);
}
