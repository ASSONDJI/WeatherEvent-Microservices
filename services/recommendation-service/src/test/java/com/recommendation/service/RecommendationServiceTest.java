package com.recommendation.service;

import com.recommendation.dto.RecommendationResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService - Tests unitaires")
class RecommendationServiceTest {

    @InjectMocks
    private RecommendationService recommendationService;

    // ========== Tests quantite ==========

    @Nested
    @DisplayName("Quantite des recommandations")
    class QuantityTests {

        @Test
        @DisplayName("doit retourner exactement 5 recommandations")
        void shouldReturnExactlyFiveRecommendations() throws Exception {
            List<RecommendationResponse> result =
                recommendationService.getRecommendations("Paris").get();

            assertThat(result).hasSize(5);
        }

        @ParameterizedTest(name = "5 recommandations pour {0}")
        @ValueSource(strings = {"Paris", "Lyon", "Marseille", "Bordeaux", "Toulouse"})
        @DisplayName("doit toujours retourner 5 recommandations quelle que soit la ville")
        void shouldAlwaysReturnFiveRecommendations(String city) throws Exception {
            List<RecommendationResponse> result =
                recommendationService.getRecommendations(city).get();

            assertThat(result).hasSize(5);
        }
    }

    // ========== Tests contenu ==========

    @Nested
    @DisplayName("Contenu des recommandations")
    class ContentTests {

        private List<RecommendationResponse> recommendations;

        @BeforeEach
        void setUp() throws Exception {
            recommendations = recommendationService.getRecommendations("Paris").get();
        }

        @Test
        @DisplayName("chaque recommandation doit avoir un id non nul et non vide")
        void eachRecommendationShouldHaveNonBlankId() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getId()).isNotBlank()
            );
        }

        @Test
        @DisplayName("chaque recommandation doit avoir une activite non vide")
        void eachRecommendationShouldHaveNonBlankActivity() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getActivity()).isNotBlank()
            );
        }

        @Test
        @DisplayName("chaque recommandation doit avoir un venue non vide")
        void eachRecommendationShouldHaveNonBlankVenue() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getVenue()).isNotBlank()
            );
        }

        @Test
        @DisplayName("chaque recommandation doit avoir une raison non vide")
        void eachRecommendationShouldHaveNonBlankReason() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getReason()).isNotBlank()
            );
        }

        @Test
        @DisplayName("chaque recommandation doit avoir un champ indoor non nul")
        void eachRecommendationShouldHaveIndoorField() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getIndoor()).isNotNull()
            );
        }

        @Test
        @DisplayName("le venue doit contenir le nom de la ville")
        void venueShouldContainCityName() throws Exception {
            String city = "Bordeaux";
            List<RecommendationResponse> result =
                recommendationService.getRecommendations(city).get();

            assertThat(result).allSatisfy(r ->
                assertThat(r.getVenue()).contains(city)
            );
        }
    }

    // ========== Tests priorites ==========

    @Nested
    @DisplayName("Priorites des recommandations")
    class PriorityTests {

        private List<RecommendationResponse> recommendations;

        @BeforeEach
        void setUp() throws Exception {
            recommendations = recommendationService.getRecommendations("Paris").get();
        }

        @Test
        @DisplayName("les priorites doivent aller de 1 a 5")
        void prioritiesShouldRangeFromOneToFive() {
            List<Integer> priorities = recommendations.stream()
                .map(RecommendationResponse::getPriority)
                .sorted()
                .collect(Collectors.toList());

            assertThat(priorities).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("toutes les priorites doivent etre uniques")
        void allPrioritiesShouldBeUnique() {
            List<Integer> priorities = recommendations.stream()
                .map(RecommendationResponse::getPriority)
                .collect(Collectors.toList());

            assertThat(priorities).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("la priorite 1 doit etre la premiere recommandation")
        void firstRecommendationShouldHavePriorityOne() {
            assertThat(recommendations.get(0).getPriority()).isEqualTo(1);
        }

        @Test
        @DisplayName("la priorite 5 doit etre la derniere recommandation")
        void lastRecommendationShouldHavePriorityFive() {
            assertThat(recommendations.get(4).getPriority()).isEqualTo(5);
        }

        @Test
        @DisplayName("toutes les priorites doivent etre positives")
        void allPrioritiesShouldBePositive() {
            assertThat(recommendations).allSatisfy(r ->
                assertThat(r.getPriority()).isPositive()
            );
        }
    }

    // ========== Tests indoor/outdoor ==========

    @Nested
    @DisplayName("Repartition indoor/outdoor")
    class IndoorOutdoorTests {

        @Test
        @DisplayName("doit avoir au moins une activite indoor")
        void shouldHaveAtLeastOneIndoorActivity() throws Exception {
            List<RecommendationResponse> result =
                recommendationService.getRecommendations("Paris").get();

            assertThat(result).anyMatch(r -> Boolean.TRUE.equals(r.getIndoor()));
        }

        @Test
        @DisplayName("doit avoir au moins une activite outdoor")
        void shouldHaveAtLeastOneOutdoorActivity() throws Exception {
            List<RecommendationResponse> result =
                recommendationService.getRecommendations("Paris").get();

            assertThat(result).anyMatch(r -> Boolean.FALSE.equals(r.getIndoor()));
        }
    }

    // ========== Tests asynchronisme ==========

    @Nested
    @DisplayName("Comportement asynchrone")
    class AsyncTests {

        @Test
        @DisplayName("doit retourner un CompletableFuture non nul")
        void shouldReturnNonNullFuture() {
            CompletableFuture<List<RecommendationResponse>> future =
                recommendationService.getRecommendations("Paris");

            assertThat(future).isNotNull();
        }

        @Test
        @DisplayName("le CompletableFuture doit se completer sans exception")
        void futureShouldCompleteWithoutException() {
            CompletableFuture<List<RecommendationResponse>> future =
                recommendationService.getRecommendations("Paris");

            assertThatNoException().isThrownBy(future::get);
        }

        @Test
        @DisplayName("deux appels successifs doivent generer des IDs differents")
        void twoCallsShouldGenerateDifferentIds() throws Exception {
            List<RecommendationResponse> first =
                recommendationService.getRecommendations("Paris").get();
            List<RecommendationResponse> second =
                recommendationService.getRecommendations("Paris").get();

            List<String> firstIds = first.stream()
                .map(RecommendationResponse::getId).collect(Collectors.toList());
            List<String> secondIds = second.stream()
                .map(RecommendationResponse::getId).collect(Collectors.toList());

            assertThat(firstIds).doesNotContainAnyElementsOf(secondIds);
        }
    }
}
