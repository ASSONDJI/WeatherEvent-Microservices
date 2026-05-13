package com.agenda.dto;

import lombok.Data;

@Data
public class BenchmarkResult {
    private Long sequentialTimeMs;
    private Long parallelTimeMs;
    private Double speedupFactor;
    private AgendaResponse sequentialResponse;
    private AgendaResponse parallelResponse;
}
