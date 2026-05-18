package com.agenda.controller;

import com.agenda.api.BenchmarkApi;
import com.agenda.dto.generated.BenchmarkResult;
import com.agenda.service.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BenchmarkController implements BenchmarkApi {

    private final AgendaService agendaService;

    @Override
    public ResponseEntity<BenchmarkResult> benchmarkAgenda(String city, LocalDate date) {
        return ResponseEntity.ok(agendaService.benchmark(city, date.toString()));
    }
}
