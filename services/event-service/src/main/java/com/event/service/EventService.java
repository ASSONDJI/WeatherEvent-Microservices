package com.event.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.CompletableFuture;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.ticketmaster.key:}")
    private String apiKey;
    
    @Value("${mock.events.enabled:false}")
    private boolean mockEnabled;

    @CircuitBreaker(name = "ticketmasterApi", fallbackMethod = "getEventsFallback")
    @Retry(name = "ticketmasterApi", fallbackMethod = "getEventsFallback")
    @TimeLimiter(name = "ticketmasterApi", fallbackMethod = "getEventsFallback")
    @Cacheable(value = "events", key = "#city + '_' + #date", unless = "#result == null || #result.isEmpty()")
    public CompletableFuture<List<Map<String, Object>>> getEvents(String city, String date) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("=== EVENTS REQUEST with Circuit Breaker and Cache ===");
            log.info("City: {}, Date: {}", city, date);
            
            if (city == null || city.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            if (mockEnabled) {
                log.info("MOCK mode enabled - returning mock events");
                return getMockEvents(city, date);
            }
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Ticketmaster API key is not configured");
            }
            
            log.info("Calling Ticketmaster API for city: {}", city);
            
            String url = String.format(
                "https://app.ticketmaster.com/discovery/v2/events.json?apikey=%s&city=%s&countryCode=FR&size=20",
                apiKey, city
            );
            
            Map<String, Object> response = webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            List<Map<String, Object>> events = parseTicketmasterResponse(response, city, date);
            log.info("Found {} real events from Ticketmaster for {}", events.size(), city);
            return events;
        });
    }
    
    public CompletableFuture<List<Map<String, Object>>> getEventsFallback(String city, String date, Throwable ex) {
        log.warn("CIRCUIT BREAKER FALLBACK - Using fallback for city: {}", city);
        log.warn("Fallback reason: {}", ex.getMessage() != null ? ex.getMessage() : "Unknown error");
        
        List<Map<String, Object>> fallbackEvents = new ArrayList<>();
        Map<String, Object> fallbackEvent = new HashMap<>();
        fallbackEvent.put("id", "fallback-001");
        fallbackEvent.put("name", "Service temporairement indisponible");
        fallbackEvent.put("location", city);
        fallbackEvent.put("date", date);
        fallbackEvent.put("price", 0);
        fallbackEvent.put("source", "Fallback");
        fallbackEvent.put("fallback", true);
        fallbackEvents.add(fallbackEvent);
        
        return CompletableFuture.completedFuture(fallbackEvents);
    }
    
    private List<Map<String, Object>> parseTicketmasterResponse(Map<String, Object> response, String city, String date) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        if (response == null) {
            return events;
        }
        
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        if (embedded == null) {
            return events;
        }
        
        List<Map<String, Object>> ticketmasterEvents = (List<Map<String, Object>>) embedded.get("events");
        if (ticketmasterEvents == null || ticketmasterEvents.isEmpty()) {
            return events;
        }
        
        for (Map<String, Object> tmEvent : ticketmasterEvents) {
            Map<String, Object> event = new HashMap<>();
            event.put("id", tmEvent.get("id"));
            event.put("name", tmEvent.get("name"));
            event.put("location", city);
            event.put("source", "Ticketmaster");
            event.put("fallback", false);
            event.put("requestedDate", date);
            
            Map<String, Object> dates = (Map<String, Object>) tmEvent.get("dates");
            if (dates != null) {
                Map<String, Object> start = (Map<String, Object>) dates.get("start");
                if (start != null && start.get("localDate") != null) {
                    event.put("eventDate", start.get("localDate"));
                }
            }
            
            List<Map<String, Object>> priceRanges = (List<Map<String, Object>>) tmEvent.get("priceRanges");
            if (priceRanges != null && !priceRanges.isEmpty()) {
                Map<String, Object> firstRange = priceRanges.get(0);
                event.put("price", firstRange.get("min") != null ? firstRange.get("min") : 0);
            } else {
                event.put("price", 0);
            }
            
            events.add(event);
        }
        
        return events;
    }
    
    private List<Map<String, Object>> getMockEvents(String city, String date) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        Map<String, Object> event1 = new HashMap<>();
        event1.put("id", 1);
        event1.put("name", "Concert de Jazz");
        event1.put("location", city);
        event1.put("date", date);
        event1.put("price", 25.0);
        event1.put("category", "Musique");
        event1.put("source", "Mock");
        events.add(event1);
        
        Map<String, Object> event2 = new HashMap<>();
        event2.put("id", 2);
        event2.put("name", "Festival de Cinema");
        event2.put("location", city);
        event2.put("date", date);
        event2.put("price", 15.0);
        event2.put("category", "Culture");
        event2.put("source", "Mock");
        events.add(event2);
        
        return events;
    }
}
