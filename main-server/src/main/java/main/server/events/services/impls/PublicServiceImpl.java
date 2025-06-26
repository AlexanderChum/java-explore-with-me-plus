package main.server.events.services.impls;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventShortDto;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.PublicService;
import main.server.exception.NotFoundException;
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
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicServiceImpl implements PublicService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    JPAQueryFactory jpaQueryFactory;
    RequestRepository requestRepository;
    StatsService statsService;

    public List<EventShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
        Integer size, HttpServletRequest request) {
        statsService.addView(request);

        List<EventModel> events = eventRepository.findAllByFiltersPublic(text, categories, paid, rangeStart, rangeEnd,
            onlyAvailable, PageRequest.of(from, size));

        Map<Long, Long> views = statsService.getAmountForEvents(events);
        return events.stream()
                .map(eventModel -> {
                    EventShortDto eventShort = eventMapper.toShortDto(eventModel);
                    eventShort.setViews(views.get(eventShort.getId()));
                    return eventShort;
                })
                .sorted((e1, e2) -> sort.equals("EVENT_DATE") ? e1.getEventDate().compareTo(e2.getEventDate()) : e1.getViews().compareTo(e2.getViews()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EventShortDto getEventById(Long eventId, HttpServletRequest request) {
        statsService.addView(request);

        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено",eventId)));

        Long views = statsService.getAmount(eventId, event.getCreatedOn(), LocalDateTime.now());

        EventShortDto result = eventMapper.toShortDto(event);
        result.setViews(views);

        return result;
    }
}
