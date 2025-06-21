package main.server.events.services;

import main.server.events.dto.EventShortDto;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getUserEvents();

    EventShortDto createEvent();

    EventShortDto getEventByEventId();

    EventShortDto updateEventByEventId();

    List<EventRequestDto> getEventRequests();

    EventRequestDto updateEventRequest();
}
