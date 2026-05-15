package com.reservation.controller;

import com.reservation.api.StatisticsApi;
import com.reservation.dto.generated.UserStats;
import com.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticsController implements StatisticsApi {

    private final ReservationService service;

    @Override
    public ResponseEntity<UserStats> getUserStats(String userId) {
        return ResponseEntity.ok(service.getUserStats(userId));
    }
}
