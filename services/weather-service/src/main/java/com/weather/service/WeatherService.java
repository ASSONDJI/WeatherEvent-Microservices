package com.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${api.openweather.key:}")
    private String apiKey;
    
    @Value("${mock.weather.enabled:true}")
    private boolean mockEnabled;

    public CompletableFuture<Map<String, Object>> getWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching weather for: {}", city);
            
            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("City cannot be null or empty");
            }
            
            // Si mode mock ou pas de clé API, retourner mock
            if (mockEnabled || apiKey == null || apiKey.isEmpty()) {
                log.info("Using MOCK weather for: {}", city);
                return getMockWeather(city);
            }
            
            // Appel à la vraie API OpenWeatherMap
            try {
                Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri("https://api.openweathermap.org/data/2.5/weather?q={city}&appid={apiKey}&units=metric&lang=fr",
                         city, apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
                
                // Transformer la réponse OpenWeatherMap en notre format
                Map<String, Object> weatherData = new HashMap<>();
                weatherData.put("city", city);
                
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
                
                java.util.List<Map<String, Object>> weather = 
                    (java.util.List<Map<String, Object>>) response.get("weather");
                if (weather != null && !weather.isEmpty()) {
                    Map<String, Object> weatherInfo = weather.get(0);
                    weatherData.put("condition", weatherInfo.get("main"));
                    weatherData.put("description", weatherInfo.get("description"));
                }
                
                weatherData.put("fallback", false);
                log.info("Real weather retrieved for {}: {}°C", city, weatherData.get("temperature"));
                return weatherData;
                
            } catch (Exception e) {
                log.error("Error fetching real weather for {}: {}, using fallback", city, e.getMessage());
                return getMockWeather(city);
            }
        });
    }
    
    private Map<String, Object> getMockWeather(String city) {
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("city", city);
        weatherData.put("temperature", 22.5);
        weatherData.put("feelsLike", 23.0);
        weatherData.put("condition", "Ensoleillé");
        weatherData.put("humidity", 65);
        weatherData.put("windSpeed", 12.3);
        weatherData.put("description", "Ciel dégagé");
        weatherData.put("fallback", true);
        
        log.info("Mock weather for {}: {}°C", city, weatherData.get("temperature"));
        return weatherData;
    }
}
