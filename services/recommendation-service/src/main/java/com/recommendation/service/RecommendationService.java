package com.recommendation.service;

import com.recommendation.dto.RecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RecommendationService {

    public CompletableFuture<List<RecommendationResponse>> getRecommendations(String city) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Generating recommendations for city: {}", city);
            return generateRecommendations(city);
        });
    }

    private List<RecommendationResponse> generateRecommendations(String city) {
        List<RecommendationResponse> recommendations = new ArrayList<>();

        recommendations.add(RecommendationResponse.builder()
            .id(UUID.randomUUID().toString().substring(0, 8))
            .activity("Visite du centre historique")
            .venue(city + " Old Town")
            .reason("Architecture magnifique et riche histoire")
            .priority(1)
            .indoor(false)
            .build());

        recommendations.add(RecommendationResponse.builder()
            .id(UUID.randomUUID().toString().substring(0, 8))
            .activity("Visite du musée local")
            .venue(city + " City Museum")
            .reason("Collections d'art et d'histoire locale")
            .priority(2)
            .indoor(true)
            .build());

        recommendations.add(RecommendationResponse.builder()
            .id(UUID.randomUUID().toString().substring(0, 8))
            .activity("Expérience culinaire locale")
            .venue(city + " Food Market")
            .reason("Plats régionaux authentiques à découvrir")
            .priority(3)
            .indoor(true)
            .build());

        recommendations.add(RecommendationResponse.builder()
            .id(UUID.randomUUID().toString().substring(0, 8))
            .activity("Balade dans le parc central")
            .venue(city + " Central Park")
            .reason("Détente et découverte de la nature locale")
            .priority(4)
            .indoor(false)
            .build());

        recommendations.add(RecommendationResponse.builder()
            .id(UUID.randomUUID().toString().substring(0, 8))
            .activity("Shopping local")
            .venue(city + " Main Shopping Street")
            .reason("Produits locaux et souvenirs authentiques")
            .priority(5)
            .indoor(true)
            .build());

        return recommendations;
    }
}
