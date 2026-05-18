package com.reservation.service;

import com.reservation.dto.generated.*;
import com.reservation.event.ReservationEventProducer;
import com.reservation.exception.InvalidReservationStatusException;
import com.reservation.exception.ReservationNotModifiableException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.model.Reservation;
import com.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Tests")
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private ReservationEventProducer eventProducer;

    @InjectMocks
    private ReservationService service;

    private Reservation reservation;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId("res-001");
        reservation.setUserId("user-123");
        reservation.setCity("Paris");
        reservation.setType("RESTAURANT");
        reservation.setName("Le Gourmet");
        reservation.setVenue("15 Rue de la Paix");
        reservation.setDate("2026-05-20");
        reservation.setNumberOfPersons(2);
        reservation.setTotalPrice(85.0);
        reservation.setStatus("PENDING");
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== CREATE ====================

    @Test
    @DisplayName("create - doit creer une reservation et publier un event")
    void create_shouldCreateReservationAndPublishEvent() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId("user-123");
        request.setCity("Paris");
        request.setType(ReservationType.RESTAURANT);
        request.setName("Le Gourmet");
        request.setVenue("15 Rue de la Paix");
        request.setNumberOfPersons(2);
        request.setTotalPrice(85.0);

        when(repository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationResponse result = service.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getCity()).isEqualTo("Paris");
        verify(repository).save(any(Reservation.class));
        verify(eventProducer).publishCreated(any(Reservation.class));
    }

    @Test
    @DisplayName("create - avec type null ne doit pas planter")
    void create_withNullType_shouldNotThrow() {
        ReservationRequest request = new ReservationRequest();
        request.setUserId("user-123");
        request.setCity("Paris");
        request.setName("Le Gourmet");

        Reservation saved = new Reservation();
        saved.setId("res-002");
        saved.setUserId("user-123");
        saved.setCity("Paris");
        saved.setName("Le Gourmet");
        saved.setStatus("PENDING");
        saved.setCreatedAt(LocalDateTime.now());
        saved.setUpdatedAt(LocalDateTime.now());

        when(repository.save(any(Reservation.class))).thenReturn(saved);

        assertThatNoException().isThrownBy(() -> service.create(request));
        verify(eventProducer).publishCreated(any(Reservation.class));
    }

    // ==================== GET BY ID ====================

    @Test
    @DisplayName("getById - doit retourner la reservation existante")
    void getById_shouldReturnReservation() {
        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));

        ReservationResponse result = service.getById("res-001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("res-001");
        assertThat(result.getCity()).isEqualTo("Paris");
    }

    @Test
    @DisplayName("getById - doit lever ResourceNotFoundException si inexistant")
    void getById_notFound_shouldThrowResourceNotFoundException() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("unknown"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== CONFIRM ====================

    @Test
    @DisplayName("confirm - doit confirmer une reservation PENDING")
    void confirm_pending_shouldConfirmReservation() {
        reservation.setStatus("PENDING");
        Reservation confirmed = new Reservation();
        confirmed.setId("res-001");
        confirmed.setUserId("user-123");
        confirmed.setCity("Paris");
        confirmed.setType("RESTAURANT");
        confirmed.setName("Le Gourmet");
        confirmed.setStatus("CONFIRMED");
        confirmed.setCreatedAt(LocalDateTime.now());
        confirmed.setUpdatedAt(LocalDateTime.now());

        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(confirmed);

        ReservationResponse result = service.confirm("res-001");

        assertThat(result.getStatus().getValue()).isEqualTo("CONFIRMED");
        verify(eventProducer).publishConfirmed(any(Reservation.class));
    }

    @Test
    @DisplayName("confirm - doit lever exception si reservation CANCELLED")
    void confirm_cancelled_shouldThrowInvalidReservationStatusException() {
        reservation.setStatus("CANCELLED");
        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.confirm("res-001"))
            .isInstanceOf(InvalidReservationStatusException.class);

        verify(eventProducer, never()).publishConfirmed(any());
    }

    // ==================== CANCEL ====================

    @Test
    @DisplayName("cancel - doit annuler une reservation PENDING")
    void cancel_pending_shouldCancelReservation() {
        reservation.setStatus("PENDING");
        Reservation cancelled = new Reservation();
        cancelled.setId("res-001");
        cancelled.setUserId("user-123");
        cancelled.setCity("Paris");
        cancelled.setType("RESTAURANT");
        cancelled.setName("Le Gourmet");
        cancelled.setStatus("CANCELLED");
        cancelled.setCreatedAt(LocalDateTime.now());
        cancelled.setUpdatedAt(LocalDateTime.now());

        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(cancelled);

        ReservationResponse result = service.cancel("res-001");

        assertThat(result.getStatus().getValue()).isEqualTo("CANCELLED");
        verify(eventProducer).publishCancelled(any(Reservation.class));
    }

    @Test
    @DisplayName("cancel - doit lever exception si deja CANCELLED")
    void cancel_alreadyCancelled_shouldThrowInvalidReservationStatusException() {
        reservation.setStatus("CANCELLED");
        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.cancel("res-001"))
            .isInstanceOf(InvalidReservationStatusException.class);

        verify(eventProducer, never()).publishCancelled(any());
    }

    // ==================== UPDATE ====================

    @Test
    @DisplayName("update - doit modifier une reservation PENDING")
    void update_pending_shouldUpdateReservation() {
        reservation.setStatus("PENDING");
        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setVenue("Nouveau Venue");
        request.setNumberOfPersons(4);

        Reservation updated = new Reservation();
        updated.setId("res-001");
        updated.setUserId("user-123");
        updated.setCity("Paris");
        updated.setType("RESTAURANT");
        updated.setName("Le Gourmet");
        updated.setVenue("Nouveau Venue");
        updated.setNumberOfPersons(4);
        updated.setStatus("PENDING");
        updated.setCreatedAt(LocalDateTime.now());
        updated.setUpdatedAt(LocalDateTime.now());

        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(updated);

        ReservationResponse result = service.update("res-001", request);

        assertThat(result).isNotNull();
        verify(repository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("update - doit lever exception si reservation CONFIRMED")
    void update_confirmed_shouldThrowReservationNotModifiableException() {
        reservation.setStatus("CONFIRMED");
        when(repository.findById("res-001")).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.update("res-001", new ReservationUpdateRequest()))
            .isInstanceOf(ReservationNotModifiableException.class);
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("delete - doit supprimer une reservation existante")
    void delete_existing_shouldDeleteReservation() {
        when(repository.existsById("res-001")).thenReturn(true);

        assertThatNoException().isThrownBy(() -> service.delete("res-001"));
        verify(repository).deleteById("res-001");
    }

    @Test
    @DisplayName("delete - doit lever exception si inexistant")
    void delete_notFound_shouldThrowResourceNotFoundException() {
        when(repository.existsById("unknown")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("unknown"))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    // ==================== GET BY USER ====================

    @Test
    @DisplayName("getByUser - doit retourner les reservations paginées")
    void getByUser_shouldReturnPagedReservations() {
        Page<Reservation> page = new PageImpl<>(List.of(reservation));
        when(repository.findByUserId(eq("user-123"), any(Pageable.class))).thenReturn(page);

        PagedReservationResponse result = service.getByUser("user-123", null, null, 0, 10, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }
}
