package br.com.apisentinel.repository;

import br.com.apisentinel.domain.DiscoveryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscoveryLogRepository extends JpaRepository<DiscoveryLog, Long> {
}
