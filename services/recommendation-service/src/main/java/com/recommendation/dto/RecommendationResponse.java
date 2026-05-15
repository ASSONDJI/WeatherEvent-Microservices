package com.recommendation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecommendationResponse {
    private String id;
    private String activity;
    private String venue;
    private String reason;
    private Integer priority;
    private Boolean indoor;
}
