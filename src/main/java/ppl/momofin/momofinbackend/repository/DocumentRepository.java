package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
