package com.agenda.dto;

import lombok.Data;
import java.util.List;

@Data
public class AgendaResponse {
    private String city;
    private String date;
    private WeatherResponse weather;
    private List<EventResponse> events;
    private List<RecommendationResponse> recommendations;
    private Long processingTimeMs;
    private String mode;
    private ApiStatus apiStatus;

    @Data
    public static class ApiStatus {
        private Boolean weatherApiAvailable;
        private Boolean eventsApiAvailable;
        private Boolean recommendationsApiAvailable;
    }
}
