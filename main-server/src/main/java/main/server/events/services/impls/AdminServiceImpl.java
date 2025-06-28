package main.server.events.services.impls;

import client.StatsClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
import main.server.exception.BadRequestException;
import main.server.exception.ConflictException;
import main.server.exception.NotFoundException;
import main.server.location.Location;
import main.server.location.LocationMapper;
import main.server.location.LocationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@SuppressWarnings("unused")
public class AdminServiceImpl implements AdminService {
    EventMapper eventMapper;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    LocationRepository locationRepository;
    LocationMapper locationMapper;
    StatsClient statsClient;

    public List<EventFullDto> getEventsWithAdminFilters(List<Long> users, List<String> states, List<Long> categories,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)) )
            throw new BadRequestException("Время начала не может быть позже времени конца");

        List<EventModel> events = eventRepository.findAllByFiltersAdmin(users, states, categories, rangeStart, rangeEnd,
                PageRequest.of(from, size));

        /*Map<Long, Long> views = statsService.getAmountForEvents(events);
        return events.stream()
                .map(eventModel -> {
                    EventFullDto eventShort = eventMapper.toFullDto(eventModel);
                    eventShort.setViews(views.get(eventShort.getId()));
                    return eventShort;
                })
                .collect(Collectors.toCollection(ArrayList::new));*/

        return events.stream()
                .map(eventModel -> {
                    EventFullDto eventFull = eventMapper.toFullDto(eventModel);
                    return eventFull;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EventFullDto updateEvent(UpdateEventAdminRequest updateRequest, Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не найдено", eventId)));

        if (updateRequest.getEventDate() != null && updateRequest.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Изменяемая дата не может быть в прошлом");
        }

        validateEventState(event, updateRequest.getState());
        changeEventState(event, updateRequest.getState());
        updateEventFields(event, updateRequest);

        EventModel updatedEvent = eventRepository.save(event);

        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        /*result.setViews(statsService.getAmount(
                eventId,
                updatedEvent.getCreatedOn(),
                LocalDateTime.now()
        ));*/

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

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Дата события не может быть в прошлом");
        }

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
