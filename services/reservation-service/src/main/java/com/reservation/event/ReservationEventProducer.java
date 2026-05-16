package com.reservation.event;

import com.reservation.model.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;
    private static final String TOPIC = "reservation-events";

    public void publishCreated(Reservation reservation) {
        publish("CREATED", reservation);
    }

    public void publishConfirmed(Reservation reservation) {
        publish("CONFIRMED", reservation);
    }

    public void publishCancelled(Reservation reservation) {
        publish("CANCELLED", reservation);
    }

    private void publish(String eventType, Reservation reservation) {
        ReservationEvent event = ReservationEvent.builder()
            .eventType(eventType)
            .reservationId(reservation.getId())
            .userId(reservation.getUserId())
            .city(reservation.getCity())
            .type(reservation.getType())
            .name(reservation.getName())
            .venue(reservation.getVenue())
            .date(reservation.getDate())
            .numberOfPersons(reservation.getNumberOfPersons())
            .totalPrice(reservation.getTotalPrice())
            .status(reservation.getStatus())
            .timestamp(LocalDateTime.now())
            .build();

        kafkaTemplate.send(TOPIC, reservation.getId(), event);
        log.info("Published {} event for reservation: {}", eventType, reservation.getId());
    }
}
