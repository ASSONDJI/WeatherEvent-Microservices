package com.agenda.controller;

import com.agenda.dto.AgendaResponse;
import com.agenda.dto.BenchmarkResult;
import com.agenda.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
@Tag(name = "Agenda", description = "Orchestration météo + événements + recommandations")
public class AgendaController {

    private final AgendaService agendaService;

    @GetMapping
    @Operation(summary = "Obtenir l'agenda complet en mode parallèle",
               description = "Combine météo, événements et recommandations en appels parallèles")
    public AgendaResponse getAgenda(
            @Parameter(description = "Nom de la ville", example = "Paris")
            @RequestParam String city,
            @Parameter(description = "Date au format YYYY-MM-DD", example = "2026-05-20")
            @RequestParam String date) {
        log.info("GET /api/agenda?city={}&date={}", city, date);
        return agendaService.buildAgendaParallel(city, date);
    }

    @GetMapping("/sequential")
    @Operation(summary = "Obtenir l'agenda en mode séquentiel")
    public AgendaResponse getAgendaSequential(
            @RequestParam String city,
            @RequestParam String date) {
        log.info("GET /api/agenda/sequential?city={}&date={}", city, date);
        return agendaService.buildAgendaSequential(city, date);
    }

    @GetMapping("/benchmark")
    @Operation(summary = "Comparer les performances séquentiel vs parallèle",
               description = "Retourne le speedup factor entre les deux modes")
    public BenchmarkResult benchmark(
            @RequestParam String city,
            @RequestParam String date) {
        log.info("GET /api/agenda/benchmark?city={}&date={}", city, date);
        return agendaService.benchmark(city, date);
    }

    @GetMapping("/test")
    @Operation(summary = "Test de santé du service")
    public String test() {
        return "Agenda Service is running!";
    }
}
