package com.recommendation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.*;

@Slf4j
@Service
public class RecommendationService {
    
    public CompletableFuture<Map<String, Object>> getRecommendations(String city) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Generating recommendations for city: {}", city);
            
            Map<String, Object> recommendations = new HashMap<>();
            recommendations.put("city", city);
            recommendations.put("restaurants", Arrays.asList(
                "Le Gourmet Parisien", 
                "Pizza Bella Italia", 
                "Sushi World", 
                "Restaurant Local"
            ));
            recommendations.put("activities", Arrays.asList(
                "Visite guidée de la ville", 
                "Musée d'histoire", 
                "Parc aquatique", 
                "Shopping center"
            ));
            recommendations.put("rating", 4.5);
            recommendations.put("season", "Toute l'année");
            
            return recommendations;
        });
    }
}
