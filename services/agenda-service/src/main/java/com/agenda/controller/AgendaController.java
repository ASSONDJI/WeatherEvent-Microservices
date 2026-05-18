package com.agenda.controller;

import com.agenda.api.AgendaApi;
import com.agenda.dto.generated.AgendaResponse;
import com.agenda.service.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AgendaController implements AgendaApi {

    private final AgendaService agendaService;

    @Override
    public ResponseEntity<AgendaResponse> getAgenda(String city, LocalDate date) {
        return ResponseEntity.ok(agendaService.buildAgendaParallel(city, date.toString()));
    }

    @Override
    public ResponseEntity<AgendaResponse> getAgendaSequential(String city, LocalDate date) {
        return ResponseEntity.ok(agendaService.buildAgendaSequential(city, date.toString()));
    }
}
