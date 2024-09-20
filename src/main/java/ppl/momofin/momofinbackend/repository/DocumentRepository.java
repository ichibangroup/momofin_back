package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ppl.momofin.momofinbackend.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findByHashString(String hashString);
}
