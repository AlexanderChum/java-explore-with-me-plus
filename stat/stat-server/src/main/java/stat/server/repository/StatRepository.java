package stat.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stat.server.model.EndpointHit;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
}