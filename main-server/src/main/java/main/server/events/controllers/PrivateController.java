package main.server.events.controllers;

import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.NewEventDto;
import main.server.events.dto.UpdateEventAdminRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/users/{userId}/events")
@Slf4j
public class PrivateController {

    @GetMapping
    public List<EventShortDto> getUserEvents(@RequestParam Long userId) {
        return null;
    }

    @PostMapping
    public EventShortDto createEvent(@RequestBody NewEventDto newEventDto,
                                @RequestParam Long userId) {
     return null;
    }

    @GetMapping("/{eventId}")
    public EventShortDto getEventByEventId(@RequestParam Long userId,
                                      @RequestParam Long eventId) {
        return null;
    }

    @PatchMapping("/{eventId}")
    public EventShortDto updateEventByEventId(@RequestParam Long userId,
                                         @RequestParam Long eventId,
                                         @RequestBody UpdateEventAdminRequest updateEventDto) {
        return null;
    }

    @GetMapping("/{eventId}/requests")
    public List<EventRequestDto> getEventRequests(@RequestParam Long userId,
                                            @RequestParam Long eventId) {
        return null;
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestDto updateEventRequest(@RequestParam Long userId,
                                              @RequestParam Long eventId,
                                              @RequestBody UpdateEventRequestDto updateEventRequestDto) {
        return null;
    }
}
