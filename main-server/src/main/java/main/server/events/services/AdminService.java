package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventAdminParams;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminService {

    List<EventShortDto> getEventsWithAdminFilters(EventAdminParams eventParams, HttpServletRequest request);

    EventShortDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);
}
