package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ppl.momofin.momofinbackend.model.DocumentVersion;
import ppl.momofin.momofinbackend.model.DocumentVersionKey;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, DocumentVersionKey> {
}
