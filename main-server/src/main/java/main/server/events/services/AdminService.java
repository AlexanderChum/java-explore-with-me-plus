package main.server.events.services;

import main.server.events.dto.EventShortDto;
import main.server.events.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminService {

    List<EventShortDto> getEvents();

    EventShortDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);
}
