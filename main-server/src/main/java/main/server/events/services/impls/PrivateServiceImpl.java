package main.server.events.services.impls;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.server.category.model.Category;
import main.server.category.repository.CategoryRepository;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.NewEventDto;
import main.server.events.dto.UpdateEventUserRequest;
import main.server.events.enums.EventState;
import main.server.events.enums.StateAction;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.PrivateService;
import main.server.exception.ConflictException;
import main.server.exception.NotFoundException;
import main.server.location.Location;
import main.server.location.LocationMapper;
import main.server.location.LocationRepository;
import main.server.statserver.StatsService;
import main.server.user.UserRepository;
import main.server.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateServiceImpl implements PrivateService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    LocationRepository locationRepository;
    LocationMapper locationMapper;

    @Transactional
    public EventFullDto createEvent(NewEventDto newEvent, Long userId) {
        User user = userExistence(userId);
        Category category = categoryExistence(newEvent.getCategory());

        Location location = locationRepository.save(locationMapper.toEntity(newEvent.getLocationDto()));

        EventModel event = eventMapper.toEntity(newEvent, category, user, location);
        /*event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());*/

        return eventMapper.toFullDto(eventRepository.save(event));
    }

    @Transactional
    public EventFullDto updateEventByEventId(UpdateEventUserRequest update, Long userId, Long eventId) {
        userExistence(userId);
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие  c id= %d не найдено", eventId)));

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Невозможно обновить опубликованное событие");
        }

        changeEventState(event, update);
        updateEventFields(event, update);

        EventModel updatedEvent = eventRepository.save(event);
        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        result.setViews(StatsService.getAmount(
                eventId,
                updatedEvent.getCreatedOn(),
                LocalDateTime.now()
        ));
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size, HttpServletRequest request) {
        userExistence(userId);
        StatsService.addView(request);

        Page<EventModel> events = eventRepository.findByInitiatorId(
                userId,
                PageRequest.of(from / size, size, Sort.by("eventDate").descending())
        );

        Map<Long, Long> views = StatsService.getAmountForEvents(events.getContent());
        return events.getContent().stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toShortDto(event);
                    dto.setViews(views.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventFullDto getEventByEventId(Long userId, Long eventId, HttpServletRequest request) {
        userExistence(userId);
        EventModel event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d " +
                                       "у пользователя с id= %d не найдено", eventId, userId)));


        StatsService.addView(request);
        EventFullDto result = eventMapper.toFullDto(event);
        result.setViews(StatsService.getAmount(
                eventId,
                event.getCreatedOn(),
                LocalDateTime.now()
        ));
        return result;
    }

    private User userExistence(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь c id= %d не найден", userId)));
    }

    private Category categoryExistence(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Категория c id= %d не найдена", categoryId)));
    }

    private void changeEventState(EventModel event, UpdateEventUserRequest update) {
        if (update.getState() != null) {
            if (update.getState() == StateAction.SEND_TO_REVIEW) event.setState(EventState.PENDING);
            if (update.getState() == StateAction.CANCEL_REVIEW) event.setState(EventState.CANCELED);
        }
    }

    private void updateEventFields(EventModel event, UpdateEventUserRequest update) {
        if (update.getAnnotation() != null) {
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getCategory() != null) {
            Category category = categoryExistence(update.getCategory());
            event.setCategory(category);
        }

        if (update.getDescription() != null) {
            event.setDescription(update.getDescription());
        }

        if (update.getEventDate() != null) {
            if (update.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Событие не может быть раньше чем через 2 часа");
            }
            event.setEventDate(update.getEventDate());
        }

        if (update.getPaid() != null) {
            event.setPaid(update.getPaid());
        }

        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }

        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }

        if (update.getTitle() != null) {
            event.setTitle(update.getTitle());
        }

        if (update.getLocation() != null) {
            event.setLocation(locationMapper.toEntity(update.getLocation()));
        }
    }
}
