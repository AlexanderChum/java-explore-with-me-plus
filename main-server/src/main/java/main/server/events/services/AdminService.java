package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventAdminParams;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminService {

    List<EventFullDto> getEventsWithAdminFilters(EventAdminParams eventParams, HttpServletRequest request);

    EventFullDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);
}
