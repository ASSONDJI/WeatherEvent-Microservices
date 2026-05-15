package com.recommendation.controller;

import com.recommendation.dto.RecommendationResponse;
import com.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/generate")
    public CompletableFuture<List<RecommendationResponse>> getRecommendations(
            @RequestParam String city) {
        log.info("Recommendation request for city: {}", city);
        return recommendationService.getRecommendations(city);
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("status", "Recommendation Service is running!",
                      "service", "recommendation-service",
                      "version", "1.0.0");
    }
}
