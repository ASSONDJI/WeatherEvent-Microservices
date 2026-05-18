package com.event.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
@DisplayName("EventService - Tests unitaires")
class EventServiceTest {

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
    private EventService eventService;

    private void setField(String name, Object value) throws Exception {
        var field = EventService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(eventService, value);
    }

    private void setupWebClientMock() {
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString()))
            .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    // ========== Tests du fallback ==========

    @Nested
    @DisplayName("getEventsFallback()")
    class FallbackTests {

        @Test
        @DisplayName("doit retourner exactement un evenement de secours")
        void shouldReturnExactlyOneFallbackEvent() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEventsFallback("Paris", "2026-05-20",
                    new RuntimeException("timeout")).get();

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("l'evenement de secours doit avoir fallback=true")
        void fallbackEventShouldHaveFallbackTrue() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEventsFallback("Paris", "2026-05-20",
                    new RuntimeException()).get();

            assertThat(result.get(0))
                .containsEntry("fallback", true)
                .containsEntry("source", "Fallback")
                .containsEntry("location", "Paris")
                .containsEntry("date", "2026-05-20");
        }

        @ParameterizedTest(name = "fallback pour {0}")
        @ValueSource(strings = {"Paris", "Lyon", "Marseille"})
        @DisplayName("doit preserver la ville et la date dans le fallback")
        void shouldPreserveCityAndDateInFallback(String city) throws Exception {
            String date = "2026-06-15";
            List<Map<String, Object>> result =
                eventService.getEventsFallback(city, date, new RuntimeException()).get();

            assertThat(result.get(0))
                .containsEntry("location", city)
                .containsEntry("date", date);
        }

        @Test
        @DisplayName("le CompletableFuture du fallback est deja complete")
        void fallbackFutureShouldBeAlreadyCompleted() {
            CompletableFuture<List<Map<String, Object>>> result =
                eventService.getEventsFallback("Paris", "2026-05-20",
                    new RuntimeException());

            assertThat(result.isDone()).isTrue();
            assertThat(result.isCompletedExceptionally()).isFalse();
        }
    }

    // ========== Tests en mode MOCK ==========

    @Nested
    @DisplayName("getEvents() - mode MOCK")
    class MockModeTests {

        @BeforeEach
        void enableMockMode() throws Exception {
            setField("mockEnabled", true);
            setField("apiKey", "");
        }

        @Test
        @DisplayName("doit retourner exactement 2 evenements mock")
        void shouldReturnTwoMockEvents() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).hasSize(2);
            verify(webClientBuilder, never()).build();
        }

        @Test
        @DisplayName("les evenements mock doivent avoir source=Mock")
        void mockEventsShouldHaveMockSource() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).allSatisfy(event ->
                assertThat(event).containsEntry("source", "Mock")
            );
        }

        @Test
        @DisplayName("les evenements mock doivent avoir une location correcte")
        void mockEventsShouldHaveCorrectLocation() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("Lyon", "2026-05-20").get();

            assertThat(result).allSatisfy(event ->
                assertThat(event).containsEntry("location", "Lyon")
            );
        }

        @Test
        @DisplayName("les evenements mock doivent avoir un prix valide")
        void mockEventsShouldHaveValidPrice() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).allSatisfy(event -> {
                assertThat(event).containsKey("price");
                assertThat(((Number) event.get("price")).doubleValue())
                    .isGreaterThanOrEqualTo(0.0);
            });
        }

        @Test
        @DisplayName("les evenements mock doivent avoir un nom non vide")
        void mockEventsShouldHaveNonEmptyName() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).allSatisfy(event -> {
                assertThat(event).containsKey("name");
                assertThat((String) event.get("name")).isNotBlank();
            });
        }
    }

    // ========== Tests ville null ou vide ==========

    @Nested
    @DisplayName("getEvents() - ville nulle ou vide")
    class EmptyCityTests {

        @BeforeEach
        void setup() throws Exception {
            setField("mockEnabled", false);
            setField("apiKey", "fake-key");
        }

        @Test
        @DisplayName("doit retourner liste vide si ville est null")
        void shouldReturnEmptyListWhenCityIsNull() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents(null, "2026-05-20").get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("doit retourner liste vide si ville est vide")
        void shouldReturnEmptyListWhenCityIsEmpty() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("", "2026-05-20").get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("doit retourner liste vide si ville ne contient que des espaces")
        void shouldReturnEmptyListWhenCityIsBlank() throws Exception {
            List<Map<String, Object>> result =
                eventService.getEvents("   ", "2026-05-20").get();

            assertThat(result).isEmpty();
        }
    }

    // ========== Tests sans cle API ==========

    @Nested
    @DisplayName("getEvents() - sans cle API")
    class NoApiKeyTests {

        @BeforeEach
        void setup() throws Exception {
            setField("mockEnabled", false);
            setField("apiKey", "");
        }

        @Test
        @DisplayName("doit lever RuntimeException si la cle API est absente")
        void shouldThrowWhenApiKeyMissing() {
            CompletableFuture<List<Map<String, Object>>> future =
                eventService.getEvents("Paris", "2026-05-20");

            assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("API key");
        }
    }

    // ========== Tests parsing reponse Ticketmaster ==========

    @Nested
    @DisplayName("getEvents() - parsing reponse Ticketmaster")
    class TicketmasterParsingTests {

        @BeforeEach
        void setup() throws Exception {
            setField("mockEnabled", false);
            setField("apiKey", "fake-api-key");
            setupWebClientMock();
        }

        @Test
        @DisplayName("doit retourner liste vide si reponse API est null")
        void shouldReturnEmptyListWhenResponseIsNull() throws Exception {
            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.justOrEmpty(null));

            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("doit retourner liste vide si _embedded est absent")
        void shouldReturnEmptyListWhenEmbeddedMissing() throws Exception {
            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("page", Map.of("totalElements", 0));

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("doit parser correctement un evenement Ticketmaster complet")
        void shouldParseCompleteTicketmasterEvent() throws Exception {
            Map<String, Object> startDate = new HashMap<>();
            startDate.put("localDate", "2026-09-15");

            Map<String, Object> dates = new HashMap<>();
            dates.put("start", startDate);

            Map<String, Object> priceRange = new HashMap<>();
            priceRange.put("min", 45.0);
            priceRange.put("max", 120.0);

            Map<String, Object> tmEvent = new HashMap<>();
            tmEvent.put("id", "tm-evt-001");
            tmEvent.put("name", "Grand Concert");
            tmEvent.put("dates", dates);
            tmEvent.put("priceRanges", List.of(priceRange));

            Map<String, Object> embedded = new HashMap<>();
            embedded.put("events", List.of(tmEvent));

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("_embedded", embedded);

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result).hasSize(1);
            Map<String, Object> event = result.get(0);
            assertThat(event)
                .containsEntry("id", "tm-evt-001")
                .containsEntry("name", "Grand Concert")
                .containsEntry("location", "Paris")
                .containsEntry("source", "Ticketmaster")
                .containsEntry("fallback", false)
                .containsEntry("eventDate", "2026-09-15")
                .containsEntry("price", 45.0);
        }

        @Test
        @DisplayName("doit mettre price=0 si priceRanges est absent")
        void shouldDefaultPriceToZeroWhenNoPriceRanges() throws Exception {
            Map<String, Object> tmEvent = new HashMap<>();
            tmEvent.put("id", "tm-evt-002");
            tmEvent.put("name", "Evenement Gratuit");

            Map<String, Object> embedded = new HashMap<>();
            embedded.put("events", List.of(tmEvent));

            Map<String, Object> apiResponse = new HashMap<>();
            apiResponse.put("_embedded", embedded);

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            List<Map<String, Object>> result =
                eventService.getEvents("Paris", "2026-05-20").get();

            assertThat(result.get(0)).containsEntry("price", 0);
        }

        @Test
        @DisplayName("doit parser plusieurs evenements correctement")
        void shouldParseMultipleEvents() throws Exception {
            List<Map<String, Object>> tmEvents = List.of(
                Map.of("id", "1", "name", "Concert A"),
                Map.of("id", "2", "name", "Concert B"),
                Map.of("id", "3", "name", "Festival C")
            );

            Map<String, Object> embedded = Map.of("events", tmEvents);
            Map<String, Object> apiResponse = Map.of("_embedded", embedded);

            when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));

            List<Map<String, Object>> result =
                eventService.getEvents("Lyon", "2026-05-20").get();

            assertThat(result).hasSize(3);
            assertThat(result).allSatisfy(event ->
                assertThat(event).containsEntry("location", "Lyon")
            );
        }
    }
}
