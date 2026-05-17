package com.reservation.service;

import com.reservation.dto.generated.*;
import com.reservation.event.ReservationEventProducer;
import com.reservation.exception.InvalidReservationStatusException;
import com.reservation.exception.ReservationNotModifiableException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.model.Reservation;
import com.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private static final String RESERVATION = "Reservation";
    private static final String PENDING = "PENDING";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String CANCELLED = "CANCELLED";

    private final ReservationRepository repository;
    private final ReservationEventProducer eventProducer;

    @Transactional
    public ReservationResponse create(ReservationRequest request) {
        log.info("Creating reservation for user: {} in {}", request.getUserId(), request.getCity());
        Reservation r = new Reservation();
        r.setUserId(request.getUserId());
        r.setCity(request.getCity());
        r.setType(request.getType() != null ? request.getType().getValue() : null);
        r.setName(request.getName());
        r.setVenue(request.getVenue());
        r.setDate(request.getDate() != null ? request.getDate().toString() : null);
        r.setNumberOfPersons(request.getNumberOfPersons() != null ? request.getNumberOfPersons() : 1);
        r.setTotalPrice(request.getTotalPrice() != null ? request.getTotalPrice() : 0.0);
        r.setNotes(request.getNotes());
        Reservation saved = repository.save(r);
        eventProducer.publishCreated(saved);
        log.info("Reservation created with id: {}", saved.getId());
        return toResponse(saved);
    }

    public PagedReservationResponse getByUser(String userId, String status, String type,
            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Reservation> result;
        if (status != null && type != null) {
            result = repository.findByUserIdAndStatusAndType(userId, status, type, pageable);
        } else if (status != null) {
            result = repository.findByUserIdAndStatus(userId, status, pageable);
        } else if (type != null) {
            result = repository.findByUserIdAndType(userId, type, pageable);
        } else {
            result = repository.findByUserId(userId, pageable);
        }
        return toPagedResponse(result);
    }

    public PagedReservationResponse search(String city, String type, String status,
            String dateFrom, String dateTo, String userId, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return toPagedResponse(repository.search(city, type, status, userId, dateFrom, dateTo, pageable));
    }

    public ReservationResponse getById(String id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ReservationResponse update(String id, ReservationUpdateRequest request) {
        Reservation r = findById(id);
        if (!PENDING.equals(r.getStatus())) {
            throw new ReservationNotModifiableException(id, r.getStatus());
        }
        if (request.getVenue() != null) r.setVenue(request.getVenue());
        if (request.getDate() != null) r.setDate(request.getDate().toString());
        if (request.getNumberOfPersons() != null) r.setNumberOfPersons(request.getNumberOfPersons());
        if (request.getTotalPrice() != null) r.setTotalPrice(request.getTotalPrice());
        if (request.getNotes() != null) r.setNotes(request.getNotes());
        log.info("Reservation updated: {}", id);
        return toResponse(repository.save(r));
    }

    @Transactional
    public ReservationResponse confirm(String id) {
        Reservation r = findById(id);
        if (CANCELLED.equals(r.getStatus())) {
            throw new InvalidReservationStatusException(r.getStatus(), CONFIRMED);
        }
        r.setStatus(CONFIRMED);
        Reservation confirmed = repository.save(r);
        eventProducer.publishConfirmed(confirmed);
        log.info("Reservation confirmed: {}", id);
        return toResponse(confirmed);
    }

    @Transactional
    public ReservationResponse cancel(String id) {
        Reservation r = findById(id);
        if (CANCELLED.equals(r.getStatus())) {
            throw new InvalidReservationStatusException(r.getStatus(), CANCELLED);
        }
        r.setStatus(CANCELLED);
        Reservation cancelled = repository.save(r);
        eventProducer.publishCancelled(cancelled);
        log.info("Reservation cancelled: {}", id);
        return toResponse(cancelled);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(RESERVATION, id);
        }
        repository.deleteById(id);
        log.info("Reservation deleted: {}", id);
    }

    public PagedReservationResponse getAllAdmin(String status, String type, String city,
            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return toPagedResponse(repository.search(city, type, status, null, null, null, pageable));
    }

    public UserStats getUserStats(String userId) {
        List<Reservation> all = repository.findByUserId(userId);
        UserStats stats = new UserStats();
        stats.setUserId(userId);
        stats.setTotalReservations(all.size());
        stats.setConfirmedReservations((int) all.stream().filter(r -> CONFIRMED.equals(r.getStatus())).count());
        stats.setCancelledReservations((int) all.stream().filter(r -> CANCELLED.equals(r.getStatus())).count());
        stats.setPendingReservations((int) all.stream().filter(r -> PENDING.equals(r.getStatus())).count());
        stats.setTotalAmountSpent(all.stream()
            .filter(r -> CONFIRMED.equals(r.getStatus()))
            .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice() : 0.0).sum());
        all.stream().collect(Collectors.groupingBy(Reservation::getCity, Collectors.counting()))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .ifPresent(e -> stats.setFavoriteCity(e.getKey()));
        all.stream().collect(Collectors.groupingBy(Reservation::getType, Collectors.counting()))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .ifPresent(e -> {
                try { stats.setFavoriteType(ReservationType.fromValue(e.getKey())); } catch (Exception ex) {
                    log.warn("Unknown reservation type: {}", e.getKey());
                }
            });
        Map<String, Integer> byType = all.stream()
            .collect(Collectors.groupingBy(Reservation::getType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
        stats.setReservationsByType(byType);
        return stats;
    }

    public GlobalStats getGlobalStats() {
        GlobalStats stats = new GlobalStats();
        stats.setTotalReservations(repository.count());
        stats.setConfirmedReservations(repository.countByStatus(CONFIRMED));
        stats.setCancelledReservations(repository.countByStatus(CANCELLED));
        stats.setPendingReservations(repository.countByStatus(PENDING));
        stats.setTotalRevenue(repository.sumTotalRevenue());
        List<String> topCities = repository.findTopCities().stream()
            .limit(5).map(r -> (String) r[0]).collect(Collectors.toList());
        stats.setTopCities(topCities);
        Map<String, Integer> byType = new HashMap<>();
        for (ReservationType type : ReservationType.values()) {
            byType.put(type.getValue(), (int) repository.countByType(type.getValue()));
        }
        stats.setReservationsByType(byType);
        Map<String, Integer> byStatus = new HashMap<>();
        byStatus.put(CONFIRMED, (int) repository.countByStatus(CONFIRMED));
        byStatus.put(CANCELLED, (int) repository.countByStatus(CANCELLED));
        byStatus.put(PENDING, (int) repository.countByStatus(PENDING));
        stats.setReservationsByStatus(byStatus);
        return stats;
    }

    private Reservation findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(RESERVATION, id));
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            Sort.Direction direction = "desc".equalsIgnoreCase(parts[1]) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(page, size, Sort.by(direction, parts[0]));
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PagedReservationResponse toPagedResponse(Page<Reservation> page) {
        PagedReservationResponse response = new PagedReservationResponse();
        response.setContent(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse res = new ReservationResponse();
        res.setId(r.getId());
        res.setUserId(r.getUserId());
        res.setCity(r.getCity());
        res.setType(r.getType() != null ? ReservationType.fromValue(r.getType()) : null);
        res.setName(r.getName());
        res.setVenue(r.getVenue());
        res.setDate(r.getDate() != null ? LocalDate.parse(r.getDate()) : null);
        res.setNumberOfPersons(r.getNumberOfPersons());
        res.setTotalPrice(r.getTotalPrice());
        res.setNotes(r.getNotes());
        res.setStatus(r.getStatus() != null ? ReservationStatus.fromValue(r.getStatus()) : null);
        res.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        res.setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
        return res;
    }
}
