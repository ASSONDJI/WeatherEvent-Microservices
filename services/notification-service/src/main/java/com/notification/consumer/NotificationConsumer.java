package com.notification.consumer;

import com.notification.event.ReservationEvent;
import com.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "reservation-events", groupId = "notification-group")
    public void handleReservationEvent(ReservationEvent event) {
        log.info("Received event: {} for reservation: {}",
            event.getEventType(), event.getReservationId());
        try {
            String type = "RESERVATION_" + event.getEventType();
            String title = buildTitle(event);
            String message = buildMessage(event);
            notificationService.save(
                event.getUserId(), type, title, message, event.getReservationId());
            log.info("Notification saved for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
        }
    }

    private String buildTitle(ReservationEvent event) {
        return switch (event.getEventType()) {
            case "CREATED" -> "Reservation created - " + event.getName();
            case "CONFIRMED" -> "Reservation confirmed - " + event.getName();
            case "CANCELLED" -> "Reservation cancelled - " + event.getName();
            default -> "Reservation update - " + event.getName();
        };
    }

    private String buildMessage(ReservationEvent event) {
        return switch (event.getEventType()) {
            case "CREATED" -> String.format(
                "Your reservation for %s at %s on %s has been created. Status: PENDING.",
                event.getName(), event.getCity(), event.getDate());
            case "CONFIRMED" -> String.format(
                "Your reservation for %s at %s on %s is CONFIRMED! Total: %.2f EUR.",
                event.getName(), event.getCity(), event.getDate(), event.getTotalPrice());
            case "CANCELLED" -> String.format(
                "Your reservation for %s at %s on %s has been CANCELLED.",
                event.getName(), event.getCity(), event.getDate());
            default -> "Your reservation has been updated.";
        };
    }
}
