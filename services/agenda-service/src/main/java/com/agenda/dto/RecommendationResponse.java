package com.agenda.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecommendationResponse {
    private String id;
    private String activity;
    private String venue;
    private String reason;
    private Integer priority;
    private Boolean indoor;
}
