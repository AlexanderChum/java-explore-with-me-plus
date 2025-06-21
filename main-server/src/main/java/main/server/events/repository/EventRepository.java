package main.server.events.repository;

import main.server.events.model.EventModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventModel, Long> {
    boolean existsByIdAndInitiatorId(Long eventId, Long initiatorId);
}
