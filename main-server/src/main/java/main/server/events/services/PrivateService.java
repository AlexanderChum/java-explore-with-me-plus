package main.server.events.services;

import main.server.events.dto.EventShortDto;
import main.server.request.dto.EventRequestStatusUpdateResultDto;
import main.server.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getUserEvents();

    EventShortDto createEvent();

    EventShortDto getEventByEventId();

    EventShortDto updateEventByEventId();

    List<ParticipationRequestDto> getEventRequests();

    EventRequestStatusUpdateResultDto updateEventRequest();
}
