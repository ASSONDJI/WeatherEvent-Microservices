package com.mashup.service;

import com.mashup.dto.generated.RecommendationResponse;
import com.mashup.dto.generated.WeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RecommendationService {

    @Cacheable(value = "recommendations", key = "#city + '_' + #weather?.condition")
    public List<RecommendationResponse> getRecommendationsCached(
            String city, WeatherResponse weather) {
        log.info("Generating recommendations for: {} - Weather: {}",
                city, weather != null ? weather.getCondition() : "unknown");
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return generateRecommendationsByWeather(city, weather);
    }


    public List<RecommendationResponse> getRecommendationsFallback(String city) {
        log.warn("Recommendation service fallback for: {}", city);
        List<RecommendationResponse> recommendations = new ArrayList<>();
        addRecommendation(recommendations, city,
                "Explore Local Attractions", city + " City Center",
                "Popular spots recommended", 1, false);
        addRecommendation(recommendations, city,
                "Local Cuisine Experience", city + " Food Market",
                "Authentic regional dishes", 2, true);
        addRecommendation(recommendations, city,
                "Art Museum Tour", city + " Art Museum",
                "Impressive collection of local artists", 3, true);
        return recommendations;
    }

    private List<RecommendationResponse> generateRecommendationsByWeather(
            String city, WeatherResponse weather) {
        List<RecommendationResponse> recommendations = new ArrayList<>();
        String condition = (weather != null && weather.getCondition() != null)
                ? weather.getCondition().toLowerCase() : "clear";

        addRecommendation(recommendations, city,
                "Visit Historic Center", city + " Old Town",
                "Beautiful architecture and rich history", 1, false);

        if (condition.contains("rain") || condition.contains("drizzle")) {
            addRecommendation(recommendations, city,
                    "Visit Local Museum", city + " City Museum",
                    "Perfect for rainy day - explore local art and history", 2, true);
            addRecommendation(recommendations, city,
                    "Shopping Center Tour", city + " Main Mall",
                    "Stay dry while shopping local products", 3, true);
            addRecommendation(recommendations, city,
                    "Coffee Shop Experience", city + " Grand Cafe",
                    "Warm up with local coffee and pastries", 4, true);
        } else if (condition.contains("clear") || condition.contains("sunny")) {
            addRecommendation(recommendations, city,
                    "Park Walking Tour", city + " Central Park",
                    "Enjoy the sunny weather in beautiful gardens", 2, false);
            addRecommendation(recommendations, city,
                    "Outdoor Market Visit", city + " Open Market",
                    "Fresh local products under the sun", 3, false);
            addRecommendation(recommendations, city,
                    "Botanical Garden", city + " Botanical Garden",
                    "Perfect day to see flowers in bloom", 4, false);
        } else if (condition.contains("cloud")) {
            addRecommendation(recommendations, city,
                    "Walking Tour", city + " City Center",
                    "Cloudy but pleasant for walking", 2, false);
            addRecommendation(recommendations, city,
                    "Indoor Sports Center", city + " Sports Complex",
                    "Stay active whatever the weather", 3, true);
            addRecommendation(recommendations, city,
                    "Art Gallery Visit", city + " Art Gallery",
                    "Perfect indoor activity for cloudy days", 4, true);
        } else if (condition.contains("snow")) {
            addRecommendation(recommendations, city,
                    "Ski Resort", city + " Mountain Resort",
                    "Perfect snow conditions for skiing", 2, false);
            addRecommendation(recommendations, city,
                    "Warm Cafe", city + " Mountain Lodge",
                    "Warm up with hot chocolate", 3, true);
            addRecommendation(recommendations, city,
                    "Indoor Ice Skating", city + " Ice Rink",
                    "Fun indoor activity for cold days", 4, true);
        } else if (condition.contains("thunderstorm")) {
            addRecommendation(recommendations, city,
                    "Cinema", city + " Movie Theater",
                    "Perfect day to watch a movie indoors", 2, true);
            addRecommendation(recommendations, city,
                    "Indoor Pool", city + " Aquatic Center",
                    "Swimming regardless of the storm", 3, true);
        }

        addRecommendation(recommendations, city,
                "Local Cuisine Experience", city + " Food Market",
                "Authentic regional dishes you must try", 5, true);

        return recommendations;
    }

    private void addRecommendation(List<RecommendationResponse> list, String city,
                                   String activity, String venue, String reason,
                                   int priority, boolean indoor) {
        RecommendationResponse rec = new RecommendationResponse();
        rec.setId(UUID.randomUUID().toString().substring(0, 8));
        rec.setActivity(activity);
        rec.setVenue(venue);
        rec.setReason(reason);
        rec.setPriority(priority);
        rec.setIndoor(indoor);
        list.add(rec);
    }
}