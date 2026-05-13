package com.agenda.service;

import com.agenda.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.weather-url}")
    private String weatherUrl;

    @Value("${services.event-url}")
    private String eventUrl;

    @Value("${services.recommendation-url}")
    private String recommendationUrl;

    public AgendaResponse buildAgendaParallel(String city, String date) {
        long startTime = System.currentTimeMillis();
        log.info("[PARALLEL] Starting agenda for {} on {}", city, date);

        CompletableFuture<WeatherResponse> weatherFuture =
            CompletableFuture.supplyAsync(() -> fetchWeather(city));
        CompletableFuture<List<EventResponse>> eventsFuture =
            CompletableFuture.supplyAsync(() -> fetchEvents(city, date));
        CompletableFuture<List<RecommendationResponse>> recommendationsFuture =
            CompletableFuture.supplyAsync(() -> fetchRecommendations(city));

        CompletableFuture.allOf(weatherFuture, eventsFuture, recommendationsFuture).join();

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("[PARALLEL] Agenda built in {}ms for {}", processingTime, city);

        return buildResponse(city, date, weatherFuture.join(),
            eventsFuture.join(), recommendationsFuture.join(),
            processingTime, "PARALLEL");
    }

    public AgendaResponse buildAgendaSequential(String city, String date) {
        long startTime = System.currentTimeMillis();
        log.info("[SEQUENTIAL] Starting agenda for {} on {}", city, date);

        WeatherResponse weather = fetchWeather(city);
        List<EventResponse> events = fetchEvents(city, date);
        List<RecommendationResponse> recommendations = fetchRecommendations(city);

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("[SEQUENTIAL] Agenda built in {}ms for {}", processingTime, city);

        return buildResponse(city, date, weather, events, recommendations,
            processingTime, "SEQUENTIAL");
    }

    public BenchmarkResult benchmark(String city, String date) {
        long seqStart = System.currentTimeMillis();
        AgendaResponse seqResponse = buildAgendaSequential(city, date);
        long seqTime = System.currentTimeMillis() - seqStart;

        long parStart = System.currentTimeMillis();
        AgendaResponse parResponse = buildAgendaParallel(city, date);
        long parTime = System.currentTimeMillis() - parStart;

        double speedup = parTime > 0 ? (double) seqTime / parTime : 1.0;

        BenchmarkResult result = new BenchmarkResult();
        result.setSequentialTimeMs(seqTime);
        result.setParallelTimeMs(parTime);
        result.setSpeedupFactor(speedup);
        result.setSequentialResponse(seqResponse);
        result.setParallelResponse(parResponse);
        return result;
    }

    private WeatherResponse fetchWeather(String city) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(weatherUrl + "/api/weather/current?city=" + city)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .block();
        } catch (Exception e) {
            log.error("Error fetching weather for {}: {}", city, e.getMessage());
            WeatherResponse fallback = new WeatherResponse();
            fallback.setCity(city);
            fallback.setCondition("Unavailable");
            fallback.setFallback(true);
            return fallback;
        }
    }

    private List<EventResponse> fetchEvents(String city, String date) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(eventUrl + "/api/events/upcoming?city=" + city + "&date=" + date)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<EventResponse>>() {})
                .block();
        } catch (Exception e) {
            log.error("Error fetching events for {}: {}", city, e.getMessage());
            return List.of();
        }
    }

    private List<RecommendationResponse> fetchRecommendations(String city) {
        try {
            return webClientBuilder.build()
                .get()
                .uri(recommendationUrl + "/api/recommendations/generate?city=" + city)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<RecommendationResponse>>() {})
                .block();
        } catch (Exception e) {
            log.error("Error fetching recommendations for {}: {}", city, e.getMessage());
            return List.of();
        }
    }

    private AgendaResponse buildResponse(String city, String date,
            WeatherResponse weather, List<EventResponse> events,
            List<RecommendationResponse> recommendations,
            long processingTime, String mode) {

        AgendaResponse response = new AgendaResponse();
        response.setCity(city);
        response.setDate(date);
        response.setWeather(weather);
        response.setEvents(events);
        response.setRecommendations(recommendations);
        response.setProcessingTimeMs(processingTime);
        response.setMode(mode);

        AgendaResponse.ApiStatus apiStatus = new AgendaResponse.ApiStatus();
        apiStatus.setWeatherApiAvailable(weather != null && !Boolean.TRUE.equals(weather.getFallback()));
        apiStatus.setEventsApiAvailable(events != null && !events.isEmpty());
        apiStatus.setRecommendationsApiAvailable(recommendations != null && !recommendations.isEmpty());
        response.setApiStatus(apiStatus);

        return response;
    }
}
