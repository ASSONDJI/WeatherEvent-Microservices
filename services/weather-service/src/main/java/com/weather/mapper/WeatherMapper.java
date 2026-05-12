package com.weather.mapper;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;
import java.time.OffsetDateTime;

@Component
public class WeatherMapper {
    
    public Map<String, Object> toWeatherResponse(Map<String, Object> weatherData) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", weatherData);
        response.put("timestamp", OffsetDateTime.now().toString());
        return response;
    }
    
    public Map<String, Object> toFallbackResponse(String city) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("city", city);
        fallback.put("status", "fallback");
        fallback.put("message", "Weather data temporarily unavailable");
        fallback.put("timestamp", OffsetDateTime.now().toString());
        
        Map<String, Object> weatherData = new HashMap<>();
        weatherData.put("city", city);
        weatherData.put("condition", "Unavailable");
        weatherData.put("temperature", 0.0);
        weatherData.put("fallback", true);
        fallback.put("data", weatherData);
        
        return fallback;
    }
}
