package com.mashup.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OpenWeatherResponse {
    private String name;
    private Main main;
    private List<Weather> weather;
    private Wind wind;

    @Data
    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        private int humidity;
    }

    @Data
    public static class Weather {
        private String main;
        private String description;
    }
    @Data
    public static class Wind {
        private double speed;
    }
}