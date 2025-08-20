package br.com.apisentinel.repository;

import br.com.apisentinel.domain.InconsistencyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InconsistencyLogRepository extends JpaRepository<InconsistencyLog, Long> {
}
