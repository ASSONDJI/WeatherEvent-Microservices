package com.weather.controller;

import com.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    public CompletableFuture<Map<String, Object>> getCurrentWeather(@RequestParam String city) {
        log.info("Weather request received for city: {}", city);
        return weatherService.getWeather(city);
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "Weather Service is running!");
    }
}
