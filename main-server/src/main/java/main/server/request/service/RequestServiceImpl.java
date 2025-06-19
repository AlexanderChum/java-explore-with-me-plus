package main.server.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.event.model.EventModel;
import main.server.event.model.EventRepository;
import main.server.exception.ConflictException;
import main.server.exception.DuplicatedDataException;
import main.server.exception.NotFoundException;
import main.server.request.RequestMapper;
import main.server.request.RequestRepository;
import main.server.request.dto.ParticipationRequestDto;
import main.server.request.model.ParticipationRequest;
import main.server.request.model.RequestStatus;
import main.server.user.UserRepository;
import main.server.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Transactional
@SuppressWarnings("unused")
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    EventRepository eventRepository;
    UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long requesterId) {
        validateUserExist(requesterId);

        return requestRepository.findAllByRequesterId(requesterId)
                .stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated).reversed())
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto createRequest(Long requesterId, Long eventId) {
        return requestMapper.toParticipationRequestDto(requestRepository.save(validateRequest(requesterId, eventId)));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long requesterId, Long requestId) {
        validateUserExist(requesterId);
        ParticipationRequest participationRequest = validateRequestExist(requesterId, requestId);

        participationRequest.setStatus(RequestStatus.CANCELED);
        return requestMapper.toParticipationRequestDto(requestRepository.save(participationRequest));
    }

    private ParticipationRequest validateRequest(Long requesterId, Long eventId) {
        User requester = validateUserExist(requesterId);
        EventModel event = validateEventExist(eventId);

        validateNotExistsByEventIdAndRequesterId(eventId, requesterId);
        if (event.getInitiator().getId().equals(requesterId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своём событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        int limit = event.getParticipantLimit();

        if (limit > 0 &&
            requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED) ==
            limit) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(requester);
        participationRequest.setEvent(event);

/////скорректировать в зависимости от типа в EventModel,
///// непонятно может ли быть уменьшен лимит после достижения максимального значения

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            participationRequest.setStatus(RequestStatus.PENDING);
        }
        return participationRequest;
    }

    private User validateUserExist(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id= %d не найден.", userId)));
    }

    private EventModel validateEventExist(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не найдено.", eventId)));
    }

    private void validateNotExistsByEventIdAndRequesterId(Long eventId, Long requesterId) {
        requestRepository.findByEventIdAndRequesterId(eventId, requesterId)
                .orElseThrow(() -> new DuplicatedDataException("Нельзя добавить повторный запрос для этого события"));
    }

    private ParticipationRequest validateRequestExist(Long requesterId, Long requestId) {
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос на событие с id= " +
                                                                       "%d не найден.", requestId)));
        if (!participationRequest.getRequester().getId().equals(requesterId)) {
            throw new ConflictException(String.format("Данный запрос с id= %d " +
                                                      "не принадлежит пользователю c id= %d", requestId, requesterId));
        }

        return participationRequest;
    }
}
