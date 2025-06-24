package main.server.events.services.impls;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.server.category.model.Category;
import main.server.category.repository.CategoryRepository;
import main.server.events.dto.EventAdminParams;
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
import main.server.location.LocationMapper;
import main.server.statserver.StatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminServiceImpl implements AdminService {
    EventMapper eventMapper;
    EventRepository eventRepository;
    CategoryRepository categoryRepository;
    LocationMapper locationMapper;

    public List<EventFullDto> getEventsWithAdminFilters(EventAdminParams eventParams, HttpServletRequest request) {
        /*StatsService.addView(request);

        //снова спроси у Андрея, аналогичный метод, с другими параметрами, возможно можно сделать одним запросным методом в репозиторий

        StatsService.getAmountForEvents(//сюда список вытащенных событий
                );

         */
        return null;
    }

    public EventFullDto updateEvent(UpdateEventAdminRequest updateRequest, Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        validateEventState(event, updateRequest.getState());
        changeEventState(event, updateRequest.getState());
        updateEventFields(event, updateRequest);

        EventModel updatedEvent = eventRepository.save(event);

        EventFullDto result = eventMapper.toFullDto(updatedEvent);
        result.setViews(StatsService.getAmount(
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
            event.setLocation(locationMapper.toEntity(updateRequest.getLocationDto()));
        }
    }
 }
