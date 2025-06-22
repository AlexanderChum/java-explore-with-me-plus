package main.server.events.services;

import main.server.events.dto.EventShortDto;

import java.util.List;

public interface PublicService {
    List<EventShortDto> getEvents();

    EventShortDto getEventById();
}
