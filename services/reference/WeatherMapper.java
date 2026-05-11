package com.mashup.mapper;

import com.mashup.dto.external.OpenWeatherResponse;
import com.mashup.dto.generated.WeatherResponse;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

@Component
public class WeatherMapper {

    public WeatherResponse toWeatherResponse(OpenWeatherResponse response) {
        if (response == null) {
            return null;
        }

        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setCity(response.getName() != null ? response.getName() : "Unknown");


        if (response.getMain() != null) {
            weatherResponse.setTemperature(response.getMain().getTemp());
            weatherResponse.setFeelsLike(response.getMain().getFeelsLike());
            weatherResponse.setHumidity(response.getMain().getHumidity());
        } else {
            weatherResponse.setTemperature(0.0);
            weatherResponse.setFeelsLike(0.0);
            weatherResponse.setHumidity(0);
        }


        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            OpenWeatherResponse.Weather weather = response.getWeather().get(0);
            weatherResponse.setCondition(weather.getMain() != null ? weather.getMain() : "Unknown");
            weatherResponse.setDescription(weather.getDescription() != null ? weather.getDescription() : "No description");
        } else {
            weatherResponse.setCondition("Unknown");
            weatherResponse.setDescription("No weather data");
        }


        if (response.getWind() != null) {
            weatherResponse.setWindSpeed(response.getWind().getSpeed());
        } else {
            weatherResponse.setWindSpeed(0.0);
        }

        weatherResponse.setFallback(false);
        weatherResponse.setCachedAt(OffsetDateTime.now());

        return weatherResponse;
    }

    public WeatherResponse toFallbackResponse(String city) {
        WeatherResponse response = new WeatherResponse();
        response.setCity(city != null ? city : "Unknown");
        response.setCondition("Unavailable");
        response.setDescription("Weather data temporarily unavailable");
        response.setFallback(true);
        response.setCachedAt(OffsetDateTime.now());
        response.setWindSpeed(0.0);
        return response;
    }
}