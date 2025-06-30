package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.EventShortDto;
import main.server.events.model.EventModel;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicService {
    EventFullDto getEventById(Long eventId, HttpServletRequest request);

    List<EventModel> findAllByCategoryId(Long catId);


    List<EventShortDto> getEventsWithFilters(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                             Integer size, HttpServletRequest request);
}
