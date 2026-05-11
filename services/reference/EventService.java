package com.mashup.service;

import com.mashup.dto.external.TicketmasterResponse;
import com.mashup.dto.generated.EventResponse;
import com.mashup.exception.ExternalApiException;
import com.mashup.mapper.EventMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final WebClient webClient;
    private final EventMapper eventMapper;

    @Value("${api.ticketmaster.key:}")
    private String apiKey;

    @Value("${mock.events.enabled:true}")
    private boolean mockEnabled;


    @CircuitBreaker(name = "eventsApi", fallbackMethod = "getEventsFallback")
    public CompletableFuture<List<EventResponse>> getEvents(String city, String date) {
        return CompletableFuture.supplyAsync(() -> getEventsCached(city, date));
    }


    @CircuitBreaker(name = "eventsApi", fallbackMethod = "getEventsCachedFallback")
    @Cacheable(value = "events", key = "#city + '_' + #date")
    public List<EventResponse> getEventsCached(String city, String date) {
        log.info("Fetching events for: {} on {}", city, date);

        if (mockEnabled || apiKey == null || apiKey.isEmpty()) {
            log.info("Using MOCK events for: {}", city);
            return getMockEvents(city);
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("app.ticketmaster.com")
                        .path("/discovery/v2/events.json")
                        .queryParam("apikey", apiKey)
                        .queryParam("city", city)
                        .queryParam("pageSize", 10)
                        .queryParam("sort", "date,asc")
                        .build())
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        Mono.error(new ExternalApiException("Ticketmaster", "/events",
                                response.statusCode().value())))
                .onStatus(status -> status.is5xxServerError(), response ->
                        Mono.error(new ExternalApiException("Ticketmaster", "/events",
                                response.statusCode().value())))
                .bodyToMono(TicketmasterResponse.class)
                .map(response -> eventMapper.toEventResponseList(response, city))
                .doOnSuccess(r -> log.info("Found {} events for {}", r.size(), city))
                .doOnError(e -> log.error("Error fetching events for {}: {}", city, e.getMessage()))
                .block();
    }


    public CompletableFuture<List<EventResponse>> getEventsFallback(
            String city, String date, Throwable ex) {
        log.warn("Circuit Breaker OPEN - fallback events for: {}", city);
        return CompletableFuture.completedFuture(eventMapper.toFallbackResponse(city));
    }


    public List<EventResponse> getEventsCachedFallback(
            String city, String date, Throwable ex) {
        log.warn("Circuit Breaker OPEN - fallback events cached for: {}", city);
        return eventMapper.toFallbackResponse(city);
    }

    private List<EventResponse> getMockEvents(String city) {
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        EventResponse e1 = new EventResponse();
        e1.setId("mock-1"); e1.setName("Mock Jazz Festival");
        e1.setVenue("Mock Theater"); e1.setCity(city);
        e1.setCategory("Music"); e1.setFallback(false);

        EventResponse e2 = new EventResponse();
        e2.setId("mock-2"); e2.setName("Mock Art Exhibition");
        e2.setVenue("Mock Museum"); e2.setCity(city);
        e2.setCategory("Art"); e2.setFallback(false);

        return List.of(e1, e2);
    }
}