package main.server.events.repository;

import main.server.events.model.EventModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventModel, Long> {
    boolean existsByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<EventModel> findAllByCategoryId(Long catId);
}