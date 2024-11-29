package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ppl.momofin.momofinbackend.model.AuditTrail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long>, JpaSpecificationExecutor<AuditTrail> {
}