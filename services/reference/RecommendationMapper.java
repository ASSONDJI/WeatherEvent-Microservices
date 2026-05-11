package com.mashup.mapper;

import com.mashup.dto.generated.RecommendationResponse;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class RecommendationMapper {

    public List<RecommendationResponse> generateRecommendations(String city) {
        List<RecommendationResponse> recommendations = new ArrayList<>();

        RecommendationResponse rec1 = new RecommendationResponse();
        rec1.setId(UUID.randomUUID().toString().substring(0, 8));
        rec1.setActivity("Visit Historic Center");
        rec1.setVenue(city + " Old Town");
        rec1.setReason("Beautiful architecture and rich history");
        rec1.setPriority(1);
        rec1.setIndoor(false);
        recommendations.add(rec1);

        RecommendationResponse rec2 = new RecommendationResponse();
        rec2.setId(UUID.randomUUID().toString().substring(0, 8));
        rec2.setActivity("Local Cuisine Experience");
        rec2.setVenue(city + " Food Market");
        rec2.setReason("Authentic regional dishes");
        rec2.setPriority(2);
        rec2.setIndoor(true);
        recommendations.add(rec2);

        RecommendationResponse rec3 = new RecommendationResponse();
        rec3.setId(UUID.randomUUID().toString().substring(0, 8));
        rec3.setActivity("Art Museum Tour");
        rec3.setVenue(city + " Art Museum");
        rec3.setReason("Impressive collection of local artists");
        rec3.setPriority(3);
        rec3.setIndoor(true);
        recommendations.add(rec3);

        return recommendations;
    }

    public List<RecommendationResponse> generateFallbackRecommendations(String city) {
        List<RecommendationResponse> recommendations = new ArrayList<>();

        RecommendationResponse fallback = new RecommendationResponse();
        fallback.setId("fallback-1");
        fallback.setActivity("Explore Local Attractions");
        fallback.setVenue(city + " City Center");
        fallback.setReason("Popular spots recommended (fallback mode)");
        fallback.setPriority(1);
        fallback.setIndoor(false);
        recommendations.add(fallback);

        return recommendations;
    }
}