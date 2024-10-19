package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ppl.momofin.momofinbackend.model.Document;
import ppl.momofin.momofinbackend.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByHashString(String hashString);
    Optional<Document> findByDocumentId(Long documentId);
    List<Document> findAllByOwner(User user);
}
