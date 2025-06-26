package main.server.events.repository;

import main.server.events.model.EventModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventModel, Long> {
    boolean existsByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<EventModel> findAllByCategoryId(Long catId);

    Page<EventModel> findByInitiatorId(Long userId, PageRequest eventDate);

    Optional<EventModel> findByIdAndInitiatorId(Long eventId, Long userId);

    @Query("""
            SELECT e
            FROM EventModel AS e
            WHERE e.state = PUBLISHED
            AND (e.annotation ILIKE %?1% or e.description ILIKE %?1%)
            AND (e.category.id in ?2)
            AND (e.paid = ?3)
            AND (e.eventDate >= CURRENT_TIMESTAMP or e.eventDate >= ?4)
            AND (e.eventDate < ?5)
            AND (?6 = false or e.participantLimit = 0 or e.participantLimit < e.confirmedRequests)
        """)
    List<EventModel> findAllByFiltersPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
        LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable pageable);

    @Query("""
            SELECT e
            FROM EventModel AS e
            WHERE (e.initiator.id IN ?1)
            AND (e.state IN ?2)
            AND (e.category.id in ?3)
            AND (e.eventDate >= ?4)
            AND (e.eventDate < ?5)
        """)
    List<EventModel> findAllByFiltersAdmin(List<Long> users, List<String> states, List<Long> categories,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);
}