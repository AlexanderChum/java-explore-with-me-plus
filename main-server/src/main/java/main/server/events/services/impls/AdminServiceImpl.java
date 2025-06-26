package main.server.events.services.impls;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.category.model.Category;
import main.server.category.repository.CategoryRepository;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.UpdateEventAdminRequest;
import main.server.events.enums.EventState;
import main.server.events.enums.StateActionAdmin;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.AdminService;
import main.server.exception.ConflictException;
import main.server.exception.NotFoundException;
import main.server.location.Location;
import main.server.location.LocationMapper;
import main.server.location.LocationRepository;
import main.server.request.RequestRepository;
import main.server.statserver.StatsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminServiceImpl implements AdminService {
    EventMapper eventMapper;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    LocationRepository locationRepository;
    LocationMapper locationMapper;
    JPAQueryFactory jpaQueryFactory;
    RequestRepository requestRepository;
    StatsService statsService;

    public List<EventFullDto> getEventsWithAdminFilters(List<Long> users, List<String> states, List<Long> categories,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size, HttpServletRequest request) {
        statsService.addView(request);

        List<EventModel> events = eventRepository.findAllByFiltersAdmin(users, states, categories, rangeStart, rangeEnd,
                PageRequest.of(from, size));

        Map<Long, Long> views = statsService.getAmountForEvents(events);
        return events.stream()
                .map(eventModel -> {
                    EventFullDto eventShort = eventMapper.toFullDto(eventModel);
                    eventShort.setViews(views.get(eventShort.getId()));
                    return eventShort;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EventFullDto updateEvent(UpdateEventAdminRequest updateRequest, Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не найдено", eventId)));

        validateEventState(event, updateRequest.getState());
        changeEventState(event, updateRequest.getState());
        updateEventFields(event, updateRequest);

        EventModel updatedEvent = eventRepository.save(event);

        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        result.setViews(statsService.getAmount(
                eventId,
                updatedEvent.getCreatedOn(),
                LocalDateTime.now()
        ));


        return result;
    }

    private void validateEventState(EventModel event, StateActionAdmin state) {
        if (state == null) return;

        if (state == StateActionAdmin.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConflictException("Только события в статусе ожидание могут быть опубликованы");
        }
        if (state == StateActionAdmin.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Только неопубликованные события могут быть отменены");
        }
    }

    private void changeEventState(EventModel event, StateActionAdmin state) {
        if (state == null) return;

        if (state == StateActionAdmin.PUBLISH_EVENT) {
            if ((event.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))) {
                throw new ConflictException("Время старта события должно быть позже");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        }

        if (state == StateActionAdmin.REJECT_EVENT) {
            event.setState(EventState.CANCELED);
        }
    }

    private void updateEventFields(EventModel event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getLocationDto() != null) {
            Location newLocation = locationMapper.toEntity(updateRequest.getLocationDto());
            locationRepository.save(newLocation);
            event.setLocation(newLocation);
        }
    }
 }
