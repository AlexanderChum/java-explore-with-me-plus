package main.server.events.controllers;

import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventShortDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/events")
@Slf4j
public class PublicController {

    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public List<EventShortDto> getEvents() {
        return null;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventShortDto getEvent(@RequestParam Long eventId) {
        return null;
    }
}
