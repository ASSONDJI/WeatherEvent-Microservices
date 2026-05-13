package com.agenda.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private String city;
    private String condition;
    private Double temperature;
    private Double feelsLike;
    private Integer humidity;
    private String description;
    private Boolean fallback;
    private Double windSpeed;
}
