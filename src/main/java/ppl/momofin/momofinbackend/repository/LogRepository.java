package ppl.momofin.momofinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ppl.momofin.momofinbackend.model.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
}
