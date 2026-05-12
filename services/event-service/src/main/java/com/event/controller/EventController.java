package com.event.controller;

import com.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping("/upcoming")
    public CompletableFuture<List<Map<String, Object>>> getUpcomingEvents(
            @RequestParam String city, 
            @RequestParam String date) {
        log.info("Events request for city: {}, date: {}", city, date);
        return eventService.getEvents(city, date);
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "Event Service is running!", 
                      "service", "event-service",
                      "version", "1.0.0");
    }
}
