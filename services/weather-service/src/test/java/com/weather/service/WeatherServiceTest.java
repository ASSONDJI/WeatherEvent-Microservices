package com.weather.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeatherService - Tests unitaires")
class WeatherServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private WeatherService weatherService;

    // ========== Helpers ==========

    private void setField(String name, Object value) throws Exception {
        var field = WeatherService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(weatherService, value);
    }

    private void setupWebClientMock() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
            .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    // ========== Tests du fallback ==========

    @Nested
    @DisplayName("getWeatherFallback()")
    class FallbackTests {

        @Test
        @DisplayName("doit retourner une reponse de secours avec fallback=true")
        void shouldReturnFallbackResponse() throws Exception {
            RuntimeException cause = new RuntimeException("Circuit breaker open");

            CompletableFuture<Map<String, Object>> result =
                weatherService.getWeatherFallback("Paris", cause);

            Map<String, Object> data = result.get();
            assertThat(data)
                .containsEntry("city", "Paris")
                .containsEntry("fallback", true)
                .containsEntry("condition", "Service indisponible")
                .containsKey("temperature");
        }

        @Test
        @DisplayName("doit preserver le nom de la ville dans le fallback")
        void shouldPreserveCityInFallback() throws Exception {
            CompletableFuture<Map<String, Object>> result =
                weatherService.getWeatherFallback("Lyon", new RuntimeException("timeout"));

            assertThat(result.get()).containsEntry("city", "Lyon");
        }

        @ParameterizedTest(name = "fallback pour la ville: {0}")
        @ValueSource(strings = {"Paris", "Lyon", "Marseille", "Toulouse"})
        @DisplayName("doit fonctionner pour toute ville")
        void shouldHandleAnyCity(String city) throws Exception {
            CompletableFuture<Map<String, Object>> result =
                weatherService.getWeatherFallback(city, new RuntimeException("error"));

            Map<String, Object> data = result.get();
            assertThat(data).containsEntry("city", city);
            assertThat((Boolean) data.get("fallback")).isTrue();
        }

        @Test
        @DisplayName("le CompletableFuture du fallback est deja complete")
        void fallbackFutureShouldBeAlreadyCompleted() {
            CompletableFuture<Map<String, Object>> result =
                weatherService.getWeatherFallback("Paris", new RuntimeException());

            assertThat(result.isDone()).isTrue();
            assertThat(result.isCompletedExceptionally()).isFalse();
        }
    }

    // ========== Tests en mode MOCK ==========

    @Nested
    @DisplayName("getWeather() - mode MOCK")
    class MockModeTests {

        @BeforeEach
        void enableMockMode() throws Exception {
            setField("mockEnabled", true);
            setField("apiKey", "");
        }

        @Test
        @DisplayName("doit retourner les donnees mock sans appeler l'API externe")
        void shouldReturnMockDataWithoutCallingApi() throws Exception {
            CompletableFuture<Map<String, Object>> future = weatherService.getWeather("Paris");
            Map<String, Object> result = future.get();

            assertThat(result)
                .containsEntry("city", "Paris")
                .containsEntry("condition", "Ensoleillé")
                .containsKey("temperature")
                .containsKey("description");
            assertThat((Boolean) result.get("fallback")).isFalse();

            // Verifier que WebClient n'est pas appele
            verify(webClientBuilder, never()).build();
        }

        @Test
        @DisplayName("les donnees mock doivent avoir une temperature valide")
        void mockDataShouldHaveValidTemperature() throws Exception {
            Map<String, Object> result = weatherService.getWeather("Lyon").get();

            assertThat(result.get("temperature"))
                .isNotNull()
                .isInstanceOf(Number.class);
            assertThat(((Number) result.get("temperature")).doubleValue())
                .isGreaterThan(-50.0)
                .isLessThan(60.0);
        }

        @ParameterizedTest(name = "mock pour {0}")
        @ValueSource(strings = {"Paris", "Lyon", "Nice", "Bordeaux"})
        @DisplayName("doit retourner le bon nom de ville pour chaque requete")
        void shouldReturnCorrectCityForEachRequest(String city) throws Exception {
            Map<String, Object> result = weatherService.getWeather(city).get();
            assertThat(result).containsEntry("city", city);
        }
    }

    // ========== Tests sans cle API ==========

    @Nested
    @DisplayName("getWeather() - sans cle API")
    class NoApiKeyTests {

        @BeforeEach
        void disableMockAndRemoveApiKey() throws Exception {
            setField("mockEnabled", false);
            setField("apiKey", "");
        }

        @Test
        @DisplayName("doit lever RuntimeException si la cle API est absente")
        void shouldThrowWhenApiKeyMissing() {
            CompletableFuture<Map<String, Object>> future = weatherService.getWeather("Paris");

            assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("API key");
        }
    }

    // ========== Tests avec cle API et WebClient ==========

    @Nested
    @DisplayName("getWeather() - avec API reelle mockee")
    class RealApiMockedTests {

        @BeforeEach
        void setup() throws Exception {
            setField("mockEnabled", false);
            setField("apiKey", "fake-api-key-for-test");
            setupWebClientMock();
        }

        @Test
        @DisplayName("doit extraire correctement les donnees de la reponse API")
        void shouldExtractWeatherDataCorrectly() throws Exception {
            Map<String, Object> main = new HashMap<>();
            main.put("temp", 18.5);
            main.put("feels_like", 16.0);
            main.put("humidity", 75);

            Map<String, Object> wind = new HashMap<>();
            wind.put("speed", 3.5);

            Map<String, Object> weatherInfo = new HashMap<>();
            weatherInfo.put("main", "Clouds");
            weatherInfo.put("description", "nuageux");

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("main", main);
            apiResponse.put("wind", wind);
            apiResponse.put("weather", List.of(weatherInfo));

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            Map<String, Object> result = weatherService.getWeather("Paris").get();

            assertThat(result)
                .containsEntry("city", "Paris")
                .containsEntry("temperature", 18.5)
                .containsEntry("feelsLike", 16.0)
                .containsEntry("humidity", 75)
                .containsEntry("windSpeed", 3.5)
                .containsEntry("condition", "Clouds")
                .containsEntry("description", "nuageux")
                .containsEntry("fallback", false);
        }

        @Test
        @DisplayName("doit utiliser getMockWeather si la reponse API est null")
        void shouldFallbackToMockWhenResponseNull() throws Exception {
            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.justOrEmpty(null));

            Map<String, Object> result = weatherService.getWeather("Paris").get();

            assertThat(result).isNotNull();
            assertThat(result).containsEntry("city", "Paris");
        }

        @Test
        @DisplayName("doit ignorer les champs absents sans lever d'exception")
        void shouldHandlePartialApiResponse() throws Exception {
            Map<String, Object> apiResponse = new HashMap<>();
            // Response sans 'main', 'wind', 'weather'
            apiResponse.put("name", "Paris");

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            assertThatNoException().isThrownBy(() ->
                weatherService.getWeather("Paris").get());
        }
    }
}
