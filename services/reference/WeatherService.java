package com.mashup.service;

import com.mashup.dto.external.OpenWeatherResponse;
import com.mashup.dto.generated.WeatherResponse;
import com.mashup.exception.CityNotFoundException;
import com.mashup.exception.ExternalApiException;
import com.mashup.mapper.WeatherMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final WeatherMapper weatherMapper;

    @Value("${api.openweather.key:}")
    private String apiKey;

    @Value("${mock.weather.enabled:true}")
    private boolean mockEnabled;


    @CircuitBreaker(name = "weatherApi", fallbackMethod = "getWeatherFallback")
    public CompletableFuture<WeatherResponse> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> getWeatherCached(city));
    }


    @CircuitBreaker(name = "weatherApi", fallbackMethod = "getWeatherCachedFallback")
    @Cacheable(value = "weather", key = "#city")
    public WeatherResponse getWeatherCached(String city) {
        log.info("Fetching weather for: {}", city);

        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }

        if (mockEnabled || apiKey == null || apiKey.isEmpty()) {
            log.info("Using MOCK weather for: {}", city);
            return getMockWeather(city);
        }

        return webClient.get()
                .uri("https://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units=metric",
                        city, apiKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().value() == 404) {
                        return Mono.error(new CityNotFoundException(city));
                    }
                    return Mono.error(new ExternalApiException("OpenWeatherMap", "/weather",
                            response.statusCode().value()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("OpenWeatherMap", "/weather",
                                response.statusCode().value())))
                .bodyToMono(OpenWeatherResponse.class)
                .map(weatherMapper::toWeatherResponse)
                .doOnSuccess(r -> log.info("Weather retrieved for {}: {}°C", city, r.getTemperature()))
                .doOnError(e -> log.error("Error fetching weather for {}: {}", city, e.getMessage()))
                .block();
    }


    public CompletableFuture<WeatherResponse> getWeatherFallback(String city, Throwable ex) {
        log.warn("Circuit Breaker OPEN - fallback weather for: {}", city);
        return CompletableFuture.completedFuture(weatherMapper.toFallbackResponse(city));
    }


    public WeatherResponse getWeatherCachedFallback(String city, Throwable ex) {
        log.warn("Circuit Breaker OPEN - fallback weather cached for: {}", city);
        return weatherMapper.toFallbackResponse(city);
    }

    private WeatherResponse getMockWeather(String city) {
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        WeatherResponse response = new WeatherResponse();
        response.setCity(city);
        response.setCondition("Sunny");
        response.setTemperature(22.5);
        response.setFeelsLike(23.0);
        response.setHumidity(65);
        response.setDescription("Mock weather data");
        response.setFallback(false);
        return response;
    }
}