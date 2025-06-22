package main.server.events.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import main.server.events.dto.EventShortDto;
import main.server.events.dto.UpdateEventAdminRequest;
import main.server.events.services.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/admin/events")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AdminController {
    AdminService adminService;

    @PostMapping("/{eventId}")
    public EventShortDto updateEvent(@Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                                     @RequestParam @Positive Long eventId) {
        log.info("Получен запрос на обновление события у админа");
        return adminService.updateEvent(updateEventAdminRequest, eventId);
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(required = false) List<Long> users,
                                         @RequestParam(required = false) List<String> states,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "0") Integer from,
                                         @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поступил запрос на обновление события для админа");
        return adminService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}