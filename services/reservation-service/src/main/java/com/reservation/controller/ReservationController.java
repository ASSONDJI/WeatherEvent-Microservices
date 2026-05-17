package com.reservation.controller;

import com.reservation.api.ReservationsApi;
import com.reservation.dto.generated.*;
import com.reservation.service.ReservationService;
import com.reservation.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReservationController implements ReservationsApi {

    private final ReservationService service;
    private final JwtUtils jwtUtils;
    private final HttpServletRequest httpRequest;

    @Override
    public ResponseEntity<ReservationResponse> createReservation(ReservationRequest request) {
        // Extraire userId du token JWT si non fourni
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            request.setUserId(jwtUtils.extractUserId(httpRequest));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Override
    public ResponseEntity<PagedReservationResponse> getReservationsByUser(
            String userId, ReservationStatus status, ReservationType type,
            Integer page, Integer size, String sort) {
        // Si userId = "me", utiliser celui du token
        String resolvedUserId = "me".equals(userId) ?
            jwtUtils.extractUserId(httpRequest) : userId;
        return ResponseEntity.ok(service.getByUser(
            resolvedUserId,
            status != null ? status.getValue() : null,
            type != null ? type.getValue() : null,
            page != null ? page : 0,
            size != null ? size : 10,
            sort != null ? sort : "createdAt,desc"));
    }

    @Override
    public ResponseEntity<PagedReservationResponse> searchReservations(
            String city, ReservationType type, ReservationStatus status,
            LocalDate dateFrom, LocalDate dateTo,
            String userId, Integer page, Integer size, String sort) {
        return ResponseEntity.ok(service.search(
            city,
            type != null ? type.getValue() : null,
            status != null ? status.getValue() : null,
            dateFrom != null ? dateFrom.toString() : null,
            dateTo != null ? dateTo.toString() : null,
            userId,
            page != null ? page : 0,
            size != null ? size : 10,
            sort != null ? sort : "createdAt,desc"));
    }

    @Override
    public ResponseEntity<ReservationResponse> getReservationById(String id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Override
    public ResponseEntity<ReservationResponse> updateReservation(
            String id, ReservationUpdateRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Override
    public ResponseEntity<ReservationResponse> confirmReservation(String id) {
        return ResponseEntity.ok(service.confirm(id));
    }

    @Override
    public ResponseEntity<ReservationResponse> cancelReservation(String id) {
        return ResponseEntity.ok(service.cancel(id));
    }

    @Override
    public ResponseEntity<Void> deleteReservation(String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
