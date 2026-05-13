package com.agenda.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {
    private String id;
    private String name;
    private String venue;
    private String city;
    private String category;
    private Boolean fallback;
    private String eventDate;
    private Double price;
    private String source;
}
