package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventPublicParams;
import main.server.events.dto.EventShortDto;

import java.util.List;

public interface PublicService {
    List<EventShortDto> getEventsWithFilters(EventPublicParams eventParams, HttpServletRequest request);

    EventShortDto getEventById(Long eventId, HttpServletRequest request);
}
