package com.mashup.service;

import com.mashup.dto.generated.AgendaResponse;
import com.mashup.dto.generated.ApiStatus;
import com.mashup.dto.generated.EventResponse;
import com.mashup.dto.generated.ModeEnum;
import com.mashup.dto.generated.RecommendationResponse;
import com.mashup.dto.generated.WeatherResponse;
import com.mashup.exception.CityNotFoundException;
import com.mashup.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgendaService {

    private final WeatherService weatherService;
    private final EventService eventService;
    private final RecommendationService recommendationService;

    public AgendaResponse buildAgendaParallel(String city, String date) {
        long startTime = System.currentTimeMillis();
        log.info("[PARALLEL] Starting agenda for {} on {}", city, date);

        try {
            // ✅ Appel direct à getWeatherCached() et getEventsCached()
            // pour que @Cacheable soit intercepté par le proxy Spring
            CompletableFuture<WeatherResponse> weatherFuture =
                    CompletableFuture.supplyAsync(() -> weatherService.getWeatherCached(city));
            CompletableFuture<List<EventResponse>> eventsFuture =
                    CompletableFuture.supplyAsync(() -> eventService.getEventsCached(city, date));

            WeatherResponse weather = weatherFuture.join();

            CompletableFuture<List<RecommendationResponse>> recommendationsFuture =
                    CompletableFuture.supplyAsync(() ->
                            recommendationService.getRecommendationsCached(city, weather));

            CompletableFuture.allOf(eventsFuture, recommendationsFuture).join();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("[PARALLEL] Agenda built in {}ms for {}", processingTime, city);

            AgendaResponse response = new AgendaResponse();
            response.setCity(city);
            response.setDate(date);
            response.setWeather(weather);
            response.setEvents(eventsFuture.join());
            response.setRecommendations(recommendationsFuture.join());
            response.setProcessingTimeMs(processingTime);
            response.setMode(ModeEnum.PARALLEL);

            ApiStatus apiStatus = new ApiStatus();
            apiStatus.setWeatherApiAvailable(!response.getWeather().getFallback());
            apiStatus.setEventsApiAvailable(!response.getEvents().isEmpty());
            apiStatus.setRecommendationsApiAvailable(true);
            response.setApiStatus(apiStatus);

            return response;

        } catch (Exception e) {
            log.error("[PARALLEL] Error for {}: {}", city, e.getMessage());
            if (e.getCause() instanceof CityNotFoundException) {
                throw (CityNotFoundException) e.getCause();
            }
            if (e.getCause() instanceof ExternalApiException) {
                throw (ExternalApiException) e.getCause();
            }
            throw new RuntimeException("Failed to build agenda for " + city, e);
        }
    }

    public AgendaResponse buildAgendaSequential(String city, String date) {
        long startTime = System.currentTimeMillis();
        log.info("[SEQUENTIAL] Starting agenda for {} on {}", city, date);

        try {
            // ✅ Appel direct à getWeatherCached() et getEventsCached()
            WeatherResponse weather = weatherService.getWeatherCached(city);
            List<EventResponse> events = eventService.getEventsCached(city, date);
            List<RecommendationResponse> recommendations =
                    recommendationService.getRecommendationsCached(city, weather);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("[SEQUENTIAL] Agenda built in {}ms for {}", processingTime, city);

            AgendaResponse response = new AgendaResponse();
            response.setCity(city);
            response.setDate(date);
            response.setWeather(weather);
            response.setEvents(events);
            response.setRecommendations(recommendations);
            response.setProcessingTimeMs(processingTime);
            response.setMode(ModeEnum.SEQUENTIAL);

            ApiStatus apiStatus = new ApiStatus();
            apiStatus.setWeatherApiAvailable(!weather.getFallback());
            apiStatus.setEventsApiAvailable(!events.isEmpty());
            apiStatus.setRecommendationsApiAvailable(true);
            response.setApiStatus(apiStatus);

            return response;

        } catch (Exception e) {
            log.error("[SEQUENTIAL] Error for {}: {}", city, e.getMessage());
            if (e.getCause() instanceof CityNotFoundException) {
                throw (CityNotFoundException) e.getCause();
            }
            if (e.getCause() instanceof ExternalApiException) {
                throw (ExternalApiException) e.getCause();
            }
            throw new RuntimeException("Failed to build sequential agenda for " + city, e);
        }
    }

    public double calculateSpeedup(long sequentialTime, long parallelTime) {
        if (sequentialTime < 0 || parallelTime < 0) {
            log.warn("Temps négatifs détectés - seq: {}ms, par: {}ms", sequentialTime, parallelTime);
            return 0.0;
        }
        if (parallelTime == 0) {
            return sequentialTime == 0 ? 1.0 : (double) sequentialTime;
        }
        double speedup = (double) sequentialTime / parallelTime;
        if (Double.isInfinite(speedup) || Double.isNaN(speedup)) {
            log.warn("Speedup invalide - seq: {}ms, par: {}ms", sequentialTime, parallelTime);
            return 0.0;
        }
        return speedup;
    }
}