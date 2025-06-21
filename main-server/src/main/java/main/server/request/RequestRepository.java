package main.server.request;

import main.server.request.model.ParticipationRequest;
import main.server.request.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);

    long countByEventIdAndStatusEquals(Long eventId, RequestStatus requestStatus);

    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    long countByIdInAndEventId(List<Long> requestIds, Long eventId);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}
