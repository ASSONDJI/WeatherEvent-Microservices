package com.weather.service;

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
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.openweather.key:}")
    private String apiKey;
    
    @Value("${mock.weather.enabled:false}")
    private boolean mockEnabled;

    @CircuitBreaker(name = "openweatherApi", fallbackMethod = "getWeatherFallback")
    @Retry(name = "openweatherApi", fallbackMethod = "getWeatherFallback")
    @TimeLimiter(name = "openweatherApi", fallbackMethod = "getWeatherFallback")
    @Cacheable(value = "weather", key = "#city", unless = "#result == null")
    public CompletableFuture<Map<String, Object>> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("=== WEATHER REQUEST with Circuit Breaker and Cache ===");
            log.info("City: {}", city);
            
            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("City cannot be null or empty");
            }
            
            if (mockEnabled) {
                log.info("MOCK mode enabled - returning mock weather");
                return getMockWeather(city);
            }
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("OpenWeatherMap API key is not configured");
            }
            
            log.info("Calling OpenWeatherMap API for city: {}", city);
            
            Map<String, Object> response = webClientBuilder.build()
                .get()
                .uri("https://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}&units=metric&lang=fr",
                     city, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            return extractWeatherData(response, city);
        });
    }
    
    public CompletableFuture<Map<String, Object>> getWeatherFallback(String city, Throwable ex) {
        log.warn("CIRCUIT BREAKER FALLBACK - Using fallback for city: {}", city);
        
        Map<String, Object> fallbackWeather = new HashMap<>();
        fallbackWeather.put("city", city);
        fallbackWeather.put("temperature", 20.0);
        fallbackWeather.put("condition", "Service indisponible");
        fallbackWeather.put("fallback", true);
        
        return CompletableFuture.completedFuture(fallbackWeather);
    }
    
    private Map<String, Object> extractWeatherData(Map<String, Object> response, String city) {
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("city", city);
        weatherData.put("fallback", false);
        
        if (response == null) {
            return getMockWeather(city);
        }
        
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        if (main != null) {
            weatherData.put("temperature", main.get("temp"));
            weatherData.put("feelsLike", main.get("feels_like"));
            weatherData.put("humidity", main.get("humidity"));
        }
        
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        if (wind != null) {
            weatherData.put("windSpeed", wind.get("speed"));
        }
        
        List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
        if (weather != null && !weather.isEmpty()) {
            Map<String, Object> weatherInfo = weather.get(0);
            weatherData.put("condition", weatherInfo.get("main"));
            weatherData.put("description", weatherInfo.get("description"));
        }
        
        return weatherData;
    }
    
    private Map<String, Object> getMockWeather(String city) {
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("city", city);
        weatherData.put("temperature", 22.5);
        weatherData.put("condition", "Ensoleillé");
        weatherData.put("description", "Ciel dégagé");
        weatherData.put("fallback", false);
        return weatherData;
    }
}
