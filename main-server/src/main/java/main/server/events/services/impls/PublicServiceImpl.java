package main.server.events.services.impls;

import client.StatsClient;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.EventShortDto;
import main.server.events.enums.EventState;
import main.server.events.mapper.EventMapper;
import main.server.events.model.EventModel;
import main.server.events.repository.EventRepository;
import main.server.events.services.PublicService;
import main.server.exception.BadRequestException;
import main.server.exception.NotFoundException;
import main.server.request.RequestRepository;
import main.server.statserver.StatsDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import stat.dto.EndpointHitDto;
import stat.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stat.constant.Const.formatter;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SuppressWarnings("unused")
public class PublicServiceImpl implements PublicService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    JPAQueryFactory jpaQueryFactory;
    RequestRepository requestRepository;
    StatsClient statsClient;

    public List<EventShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid,
    LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
    Integer size, HttpServletRequest request) {

        statsClient.postHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter)))
                .build());

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала на может быть позже окончания");

        List<EventModel> events = eventRepository.findAllByFiltersPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, (Pageable) PageRequest.of(from, size));
        Map<Long, Long> views = getAmountOfViews(events);

        return events.stream()
                .map(eventModel -> {
                    EventShortDto eventShort = eventMapper.toShortDto(eventModel);
                    eventShort.setViews(views.get(eventModel.getId()));
                    return eventShort;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        statsClient.postHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.parse(LocalDateTime.now().format(formatter)))
                .build());

        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено",eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id= %d недоступно, так как не опубликовано",eventId));
        }

        EventFullDto result = eventMapper.toFullDto(event);
        result.setViews(getAmountOfViews(List.of(event)).get(event.getId()));

        return result;
    }

    private Map<Long, Long> getAmountOfViews(List<EventModel> events) {
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime minDate = events.stream()
                .map(EventModel::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(1));

        List<ViewStatsDto> views = statsClient.getStatistics(minDate, LocalDateTime.now(), uris, true);
        return views.stream()
                .filter(dto -> dto.getUri() != null)
                .collect(Collectors.toMap(
                        dto -> {
                            String[] parts = dto.getUri().split("/");
                            if (parts.length >= 3) {
                                try {
                                    return Long.parseLong(parts[2]);
                                } catch (NumberFormatException e) {
                                    log.error("Не получилось вытащить id из uri: {}", dto.getUri());
                                }
                            }
                            return -1L;
                        },
                        ViewStatsDto::getHits,
                        (existing, replacement) -> existing
                ))
                .entrySet().stream()
                .filter(entry -> entry.getKey() != -1L)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
