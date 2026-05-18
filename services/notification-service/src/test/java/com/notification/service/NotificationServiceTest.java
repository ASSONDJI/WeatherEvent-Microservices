package com.notification.service;

import com.notification.exception.ResourceNotFoundException;
import com.notification.model.Notification;
import com.notification.repository.NotificationRepository;
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
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationService service;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId("notif-001");
        notification.setUserId("user-123");
        notification.setType("RESERVATION_CREATED");
        notification.setTitle("Réservation confirmée");
        notification.setMessage("Votre réservation a été créée");
        notification.setReservationId("res-001");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
    }

    // ==================== SAVE ====================

    @Test
    @DisplayName("save - doit sauvegarder une notification")
    void save_shouldPersistNotification() {
        when(repository.save(any(Notification.class))).thenReturn(notification);

        Notification result = service.save("user-123", "RESERVATION_CREATED",
            "Réservation confirmée", "Votre réservation a été créée", "res-001");

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getType()).isEqualTo("RESERVATION_CREATED");
        verify(repository).save(any(Notification.class));
    }

    // ==================== GET BY ID ====================

    @Test
    @DisplayName("getById - doit retourner la notification")
    void getById_shouldReturnNotification() {
        when(repository.findById("notif-001")).thenReturn(Optional.of(notification));

        var result = service.getById("notif-001");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("notif-001");
    }

    @Test
    @DisplayName("getById - doit lever ResourceNotFoundException si inexistant")
    void getById_notFound_shouldThrowException() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("unknown"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ==================== MARK AS READ ====================

    @Test
    @DisplayName("markAsRead - doit marquer comme lue")
    void markAsRead_shouldSetReadTrue() {
        notification.setRead(false);
        Notification readNotif = new Notification();
        readNotif.setId("notif-001");
        readNotif.setUserId("user-123");
        readNotif.setType("RESERVATION_CREATED");
        readNotif.setTitle("Réservation confirmée");
        readNotif.setMessage("Votre réservation a été créée");
        readNotif.setRead(true);
        readNotif.setReadAt(LocalDateTime.now());
        readNotif.setCreatedAt(LocalDateTime.now());

        when(repository.findById("notif-001")).thenReturn(Optional.of(notification));
        when(repository.save(any(Notification.class))).thenReturn(readNotif);

        var result = service.markAsRead("notif-001");

        assertThat(result.getRead()).isTrue();
        verify(repository).save(any(Notification.class));
    }

    // ==================== MARK ALL AS READ ====================

    @Test
    @DisplayName("markAllAsRead - doit retourner le nombre de notifications modifiees")
    void markAllAsRead_shouldReturnAffectedCount() {
        when(repository.markAllAsRead("user-123")).thenReturn(5);

        var result = service.markAllAsRead("user-123");

        assertThat(result.getAffectedCount()).isEqualTo(5);
        assertThat(result.getMessage()).contains("5");
    }

    // ==================== DELETE ====================

    @Test
    @DisplayName("delete - doit supprimer une notification existante")
    void delete_existing_shouldDelete() {
        when(repository.existsById("notif-001")).thenReturn(true);

        assertThatNoException().isThrownBy(() -> service.delete("notif-001"));
        verify(repository).deleteById("notif-001");
    }

    @Test
    @DisplayName("delete - doit lever exception si inexistant")
    void delete_notFound_shouldThrowException() {
        when(repository.existsById("unknown")).thenReturn(false);

        assertThatThrownBy(() -> service.delete("unknown"))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    // ==================== UNREAD COUNT ====================

    @Test
    @DisplayName("getUnreadCount - doit retourner le nombre de non-lues")
    void getUnreadCount_shouldReturnCount() {
        when(repository.countByUserIdAndRead("user-123", false)).thenReturn(3L);

        var result = service.getUnreadCount("user-123");

        assertThat(result.getUnreadCount()).isEqualTo(3L);
        assertThat(result.getUserId()).isEqualTo("user-123");
    }

    // ==================== GET BY USER ====================

    @Test
    @DisplayName("getByUser - doit retourner les notifications paginées")
    void getByUser_shouldReturnPagedNotifications() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(repository.findByUserId(eq("user-123"), any(Pageable.class))).thenReturn(page);

        var result = service.getByUser("user-123", null, null, 0, 10, null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByUser avec filtre read - doit appeler findByUserIdAndRead")
    void getByUser_withReadFilter_shouldCallCorrectRepo() {
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(repository.findByUserIdAndRead(eq("user-123"), eq(false), any(Pageable.class)))
            .thenReturn(page);

        var result = service.getByUser("user-123", false, null, 0, 10, null);

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByUserIdAndRead(eq("user-123"), eq(false), any(Pageable.class));
    }
}
