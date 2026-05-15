package com.reservation.controller;

import com.reservation.api.AdminApi;
import com.reservation.dto.generated.*;
import com.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final ReservationService service;

    @Override
    public ResponseEntity<PagedReservationResponse> getAllReservations(
            ReservationStatus status, ReservationType type, String city,
            Integer page, Integer size, String sort) {
        return ResponseEntity.ok(service.getAllAdmin(
            status != null ? status.getValue() : null,
            type != null ? type.getValue() : null,
            city,
            page != null ? page : 0,
            size != null ? size : 20,
            sort != null ? sort : "createdAt,desc"));
    }

    @Override
    public ResponseEntity<GlobalStats> getGlobalStats() {
        return ResponseEntity.ok(service.getGlobalStats());
    }
}
