package com.agenda.service;

import com.agenda.dto.generated.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendaService Tests")
class AgendaServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AgendaService service;

    @BeforeEach
    void setUp() throws Exception {
        // Injection des URLs via reflection
        setField(service, "weatherUrl", "http://weather-service:8081");
        setField(service, "eventUrl", "http://event-service:8082");
        setField(service, "recommendationUrl", "http://recommendation-service:8083");

        // Mock WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ==================== BUILD RESPONSE ====================

    @Test
    @DisplayName("buildAgendaParallel - doit retourner une reponse en mode PARALLEL")
    void buildAgendaParallel_shouldReturnParallelResponse() {
        WeatherResponse weather = new WeatherResponse();
        weather.setCity("Paris");
        weather.setCondition("Sunny");
        weather.setFallback(false);

        EventResponse event = new EventResponse();
        event.setId("evt-001");
        event.setName("Concert");

        RecommendationResponse reco = new RecommendationResponse();
        reco.setId("rec-001");

        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.just(weather));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of(event)))
            .thenReturn(Mono.just(List.of(reco)));

        AgendaResponse result = service.buildAgendaParallel("Paris", "2026-05-20");

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Paris");
        assertThat(result.getDate()).isEqualTo("2026-05-20");
        assertThat(result.getMode()).isEqualTo(ModeEnum.PARALLEL);
    }

    @Test
    @DisplayName("buildAgendaSequential - doit retourner une reponse en mode SEQUENTIAL")
    void buildAgendaSequential_shouldReturnSequentialResponse() {
        WeatherResponse weather = new WeatherResponse();
        weather.setCity("Paris");
        weather.setCondition("Cloudy");
        weather.setFallback(false);

        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.just(weather));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()));

        AgendaResponse result = service.buildAgendaSequential("Paris", "2026-05-20");

        assertThat(result).isNotNull();
        assertThat(result.getMode()).isEqualTo(ModeEnum.SEQUENTIAL);
        assertThat(result.getCity()).isEqualTo("Paris");
    }

    @Test
    @DisplayName("buildAgendaParallel - fallback si weather service indisponible")
    void buildAgendaParallel_weatherServiceDown_shouldUseFallback() {
        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.error(new RuntimeException("Service indisponible")));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()));

        AgendaResponse result = service.buildAgendaParallel("Paris", "2026-05-20");

        assertThat(result).isNotNull();
        assertThat(result.getWeather()).isNotNull();
        assertThat(result.getWeather().getFallback()).isTrue();
        assertThat(result.getApiStatus().getWeatherApiAvailable()).isFalse();
    }

    @Test
    @DisplayName("buildAgendaParallel - liste events vide si event service indisponible")
    void buildAgendaParallel_eventServiceDown_shouldReturnEmptyEvents() {
        WeatherResponse weather = new WeatherResponse();
        weather.setCity("Paris");
        weather.setFallback(false);

        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.just(weather));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.error(new RuntimeException("Event service down")))
            .thenReturn(Mono.just(List.of()));

        AgendaResponse result = service.buildAgendaParallel("Paris", "2026-05-20");

        assertThat(result.getEvents()).isEmpty();
        assertThat(result.getApiStatus().getEventsApiAvailable()).isFalse();
    }

    @Test
    @DisplayName("benchmark - doit retourner sequential ET parallel avec speedup")
    void benchmark_shouldReturnBothModesWithSpeedup() {
        WeatherResponse weather = new WeatherResponse();
        weather.setCity("Lyon");
        weather.setFallback(false);

        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.just(weather));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()));

        BenchmarkResult result = service.benchmark("Lyon", "2026-05-20");

        assertThat(result).isNotNull();
        assertThat(result.getSequentialTimeMs()).isNotNull().isGreaterThanOrEqualTo(0L);
        assertThat(result.getParallelTimeMs()).isNotNull().isGreaterThanOrEqualTo(0L);
        assertThat(result.getSpeedupFactor()).isNotNull().isGreaterThan(0.0);
        assertThat(result.getSequentialResponse()).isNotNull();
        assertThat(result.getParallelResponse()).isNotNull();
    }

    @Test
    @DisplayName("apiStatus - doit etre false si weather est fallback")
    void apiStatus_weatherFallback_shouldBeUnavailable() {
        WeatherResponse fallbackWeather = new WeatherResponse();
        fallbackWeather.setCity("Paris");
        fallbackWeather.setFallback(true);

        when(responseSpec.bodyToMono(WeatherResponse.class))
            .thenReturn(Mono.just(fallbackWeather));
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of()))
            .thenReturn(Mono.just(List.of()));

        AgendaResponse result = service.buildAgendaSequential("Paris", "2026-05-20");

        assertThat(result.getApiStatus().getWeatherApiAvailable()).isFalse();
    }
}
