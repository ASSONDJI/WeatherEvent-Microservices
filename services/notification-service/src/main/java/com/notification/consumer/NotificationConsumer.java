package com.notification.consumer;

import com.notification.event.ReservationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(topics = "reservation-events", groupId = "notification-group")
    public void handleReservationEvent(ReservationEvent event) {
        log.info("=== NOTIFICATION RECEIVED ===");
        log.info("Event Type: {}", event.getEventType());
        log.info("Reservation ID: {}", event.getReservationId());
        log.info("User: {}", event.getUserId());
        log.info("City: {}", event.getCity());
        log.info("Type: {}", event.getType());
        log.info("Name: {}", event.getName());
        log.info("Date: {}", event.getDate());
        log.info("Persons: {}", event.getNumberOfPersons());
        log.info("Price: {}€", event.getTotalPrice());
        log.info("Status: {}", event.getStatus());

        switch (event.getEventType()) {
            case "CREATED" -> sendCreationNotification(event);
            case "CONFIRMED" -> sendConfirmationNotification(event);
            case "CANCELLED" -> sendCancellationNotification(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void sendCreationNotification(ReservationEvent event) {
        log.info("📧 [EMAIL] Reservation created for user: {}", event.getUserId());
        log.info("   Dear {}, your reservation for {} at {} on {} has been created.",
            event.getUserId(), event.getName(), event.getCity(), event.getDate());
        log.info("   Reservation ID: {} | Status: PENDING", event.getReservationId());
    }

    private void sendConfirmationNotification(ReservationEvent event) {
        log.info("✅ [EMAIL] Reservation confirmed for user: {}", event.getUserId());
        log.info("   Dear {}, your reservation for {} at {} on {} is CONFIRMED!",
            event.getUserId(), event.getName(), event.getCity(), event.getDate());
        log.info("   Total: {}€ | Persons: {}", event.getTotalPrice(), event.getNumberOfPersons());
    }

    private void sendCancellationNotification(ReservationEvent event) {
        log.info("❌ [EMAIL] Reservation cancelled for user: {}", event.getUserId());
        log.info("   Dear {}, your reservation for {} at {} has been CANCELLED.",
            event.getUserId(), event.getName(), event.getCity());
    }
}
