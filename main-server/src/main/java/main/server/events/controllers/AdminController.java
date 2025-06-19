package main.server.events.controllers;

import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.UpdateEventAdminRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/admin/events")
@Slf4j
public class AdminController {

    @GetMapping
    public List<EventShortDto> getEvents() {
        return null;
    }

    @PostMapping("/{eventId}")
    public EventShortDto updateDto(@RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                   @RequestParam Long eventId) {
        return null;
    }
}
