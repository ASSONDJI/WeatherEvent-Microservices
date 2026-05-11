package com.weather.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/test")
    public String test() {
        return "Weather Service is running!";
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status", "UP",
            "service", serviceName,
            "port", "8081"
        );
    }
}
