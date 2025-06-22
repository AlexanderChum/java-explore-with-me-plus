package main.server.events.services;

import main.server.events.dto.EventShortDto;
import main.server.events.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminService {

    List<EventShortDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventShortDto updateEvent(UpdateEventAdminRequest updateEventAdminRequest, Long eventId);
}
