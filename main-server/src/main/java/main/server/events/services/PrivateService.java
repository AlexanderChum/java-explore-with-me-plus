package main.server.events.services;

import jakarta.servlet.http.HttpServletRequest;
import main.server.events.dto.EventFullDto;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.NewEventDto;
import main.server.events.dto.UpdateEventUserRequest;
import main.server.request.dto.EventRequestStatusUpdateRequestDto;
import main.server.request.dto.EventRequestStatusUpdateResultDto;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size, HttpServletRequest request);

    EventFullDto createEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getEventByEventId(Long userId, Long eventId, HttpServletRequest request);

    EventFullDto updateEventByEventId(UpdateEventUserRequest updateEventDto, Long userId, Long eventId);

    //List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateEventRequest(Long userId, Long eventId,
                                                         EventRequestStatusUpdateRequestDto update);
}
