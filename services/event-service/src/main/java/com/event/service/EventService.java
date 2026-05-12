package com.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.*;

@Slf4j
@Service
public class EventService {
    
    public CompletableFuture<List<Map<String, Object>>> getEvents(String city, String date) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching events for city: {}, date: {}", city, date);
            
            List<Map<String, Object>> events = new ArrayList<>();
            
            Map<String, Object> event1 = new HashMap<>();
            event1.put("id", 1);
            event1.put("name", "Concert de Jazz");
            event1.put("location", city);
            event1.put("date", date);
            event1.put("price", 25.0);
            event1.put("category", "Musique");
            events.add(event1);
            
            Map<String, Object> event2 = new HashMap<>();
            event2.put("id", 2);
            event2.put("name", "Festival de Cinema");
            event2.put("location", city);
            event2.put("date", date);
            event2.put("price", 15.0);
            event2.put("category", "Culture");
            events.add(event2);
            
            Map<String, Object> event3 = new HashMap<>();
            event3.put("id", 3);
            event3.put("name", "Exposition d'Art");
            event3.put("location", city);
            event3.put("date", date);
            event3.put("price", 10.0);
            event3.put("category", "Art");
            events.add(event3);
            
            return events;
        });
    }
}
