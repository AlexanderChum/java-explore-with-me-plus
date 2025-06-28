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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
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

        // Send hit to stats service
        try {
            statsClient.postHit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to send hit to stats service: {}", e.getMessage());
        }

        if ((rangeStart != null) && (rangeEnd != null) && (rangeStart.isAfter(rangeEnd)))
            throw new BadRequestException("Время начала на может быть позже окончания");

        List<EventModel> events = eventRepository.findAllByFiltersPublic(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, (Pageable) PageRequest.of(from, size));
        
        // Add a small delay to ensure stats are processed
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<Long, Long> views = getAmountOfViews(events);

        return events.stream()
                .map(eventModel -> {
                    EventShortDto eventShort = eventMapper.toShortDto(eventModel);
                    eventShort.setViews(views.getOrDefault(eventModel.getId(), 0L));
                    return eventShort;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        // Send hit to stats service
        /*try {
            String clientIp = request.getRemoteAddr();
            String requestUri = request.getRequestURI();
            log.debug("Sending hit: app=ewm-main-service, uri={}, ip={}, timestamp={}", 
                    requestUri, clientIp, LocalDateTime.now());
            
            statsClient.postHit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(requestUri)
                    .ip(clientIp)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to send hit to stats service: {}", e.getMessage());
        }*/

        try {
            statsClient.postHit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("Failed to send hit to stats service: {}", e.getMessage());
        }

        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id= %d не было найдено",eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Событие с id= %d недоступно, так как не опубликовано",eventId));
        }

        EventFullDto result = eventMapper.toFullDto(event);
        
        // Add a small delay to ensure stats are processed
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<Long, Long> viewsMap = getAmountOfViews(List.of(event));
        result.setViews(viewsMap.getOrDefault(event.getId(), 0L));

        return result;
    }

    /*private Map<Long, Long> getAmountOfViews(List<EventModel> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }

        try {
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
        } catch (Exception e) {
            log.error("Failed to get views amount: {}", e.getMessage());
            return Map.of();
        }
    }*/

    private Map<Long, Long> getAmountOfViews(List<EventModel> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .distinct()
                .collect(Collectors.toList());

        // Use a much broader time range to ensure we capture recent hits
        LocalDateTime startTime = LocalDateTime.now().minusDays(1); // Query last 24 hours
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(5); // Add buffer to capture recent hits

        Map<Long, Long> viewsMap = new HashMap<>();
        try {
            log.debug("Requesting stats for URIs: {} from {} to {}", uris, startTime, endTime);
            List<ViewStatsDto> stats = statsClient.getStatistics(
                    startTime,
                    endTime,
                    uris,
                    true // Уникальные просмотры
            );
            log.debug("Received stats: {}", stats);
            if (stats != null && !stats.isEmpty()) {
                log.debug("Processing {} stats entries", stats.size());
                for (ViewStatsDto stat : stats) {
                    try {
                        Long eventId = Long.parseLong(stat.getUri().substring("/events/".length()));
                        viewsMap.put(eventId, stat.getHits());
                        log.debug("Mapped URI {} to eventId {} with {} hits", stat.getUri(), eventId, stat.getHits());
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        log.warn("Could not parse eventId from URI {} from stats service", stat.getUri());
                    }
                }
            } else {
                log.debug("No stats returned from stats service");
            }
        } catch (Exception e) {
            log.error("Failed to retrieve views for multiple events. Error: {}", e.getMessage());
        }
        log.debug("Final views map: {}", viewsMap);
        return viewsMap;
    }
}
