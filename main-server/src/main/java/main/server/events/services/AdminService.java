package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {

    EventFullDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);

    List<EventFullDto> getEventsWithAdminFilters(List<Long> users, List<String> states, List<Long> categories,
        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);
}
