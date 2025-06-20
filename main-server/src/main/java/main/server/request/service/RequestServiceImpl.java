package main.server.request.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.event.model.EventModel;
import main.server.event.model.EventRepository;
import main.server.exception.BadRequestException;
import main.server.exception.ConflictException;
import main.server.exception.DuplicatedDataException;
import main.server.exception.NotFoundException;
import main.server.request.RequestMapper;
import main.server.request.RequestRepository;
import main.server.request.dto.EventRequestStatusUpdateRequestDto;
import main.server.request.dto.EventRequestStatusUpdateResultDto;
import main.server.request.dto.ParticipationRequestDto;
import main.server.request.model.ParticipationRequest;
import main.server.request.model.RequestStatus;
import main.server.user.UserRepository;
import main.server.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequests(Long requesterId) {
        validateUserExist(requesterId);

        return requestRepository.findAllByRequesterId(requesterId)
                .stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
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

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getCurrentUserEventRequests(Long initiatorId, Long eventId) {
        validateUserExist(initiatorId);
        validateEventExist(eventId);
////eventRepository.existsByIdAndInitiatorId если будет возвращать boolean, то оставить, если optional, то переделать

        if (!eventRepository.existsByIdAndInitiatorId(eventId, initiatorId))
            throw new ConflictException(String.format("Событие с id= %d " +
                                                      "с инициатором id= %d не найдено",eventId, initiatorId));
        return requestRepository.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .map(requestMapper::toParticipationRequestDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public EventRequestStatusUpdateResultDto updateParticipationRequestsStatus(Long initiatorId, Long eventId,
                                                                               EventRequestStatusUpdateRequestDto eventRequestStatusUpdateRequestDto) {
        validateUserExist(initiatorId);
        EventModel event = validateEventExist(eventId);

        if(!event.getInitiator().getId().equals(initiatorId)) {
            throw new ConflictException("Только инициатор события может менять статус запроса на участие в событии");
        }

        int limit = event.getParticipantLimit();

        EventRequestStatusUpdateResultDto result = new EventRequestStatusUpdateResultDto();

        if (!event.getRequestModeration() || limit == 0) {
            return result;
        }

        List<Long> requestIds = eventRequestStatusUpdateRequestDto.getRequestIds();
        RequestStatus status = eventRequestStatusUpdateRequestDto.getStatus();

        if (!status.equals(RequestStatus.REJECTED) && !status.equals(RequestStatus.CONFIRMED)) {
            throw new BadRequestException("Статус должен быть REJECTED или CONFIRMED");
        }

        if (requestRepository.countByIdInAndEventId(requestIds, eventId) != requestIds.size()) {
            throw new ConflictException(String.format("Не все запросы соответствуют событию с id= %d", eventId));
        }

        if (requestRepository
                    .countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED) >= limit) {
            throw new ConflictException(String.format("Уже достигнут лимит предела заявок на событие с id= %d", eventId));
        }

        LinkedHashMap<Long, ParticipationRequest> requestsMap = requestRepository.findAllByIdIn(requestIds)
                .stream()
                .sorted(Comparator.comparing(ParticipationRequest::getCreated))
                .collect(Collectors.toMap(
                        ParticipationRequest::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        if (requestsMap.values().stream().anyMatch(request -> request.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("У всех запросов должен быть статус: PENDING");
        }

        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();

        int confirmedCount = limit -
                             requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED);

        requestsMap.values().forEach(request -> {
            if (status == RequestStatus.REJECTED) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
            } else {
                if (confirmedRequests.size() < confirmedCount) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
                }
            }
        });


        result.getConfirmedRequests().addAll(confirmedRequests);
        result.getRejectedRequests().addAll(rejectedRequests);

        requestRepository.saveAll(requestsMap.values());

////уточнить как устанавливается количество подтвержденных заявок в event

        eventRepository.setContirmedRequests(eventId,
                requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED));

        return result;
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
            requestRepository.countByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED) == limit) {
            throw new ConflictException("Достигнут лимит запросов на участие");
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(requester);
        participationRequest.setEvent(event);

/////скорректировать в зависимости от типа в EventModel,
///// непонятно может ли быть уменьшен лимит после достижения максимального значения

        if (!event.getRequestModeration() || limit == 0) {
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
