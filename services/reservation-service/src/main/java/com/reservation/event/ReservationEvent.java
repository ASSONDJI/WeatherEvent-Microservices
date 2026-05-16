package com.reservation.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEvent {
    private String eventType;
    private String reservationId;
    private String userId;
    private String city;
    private String type;
    private String name;
    private String venue;
    private String date;
    private Integer numberOfPersons;
    private Double totalPrice;
    private String status;
    private LocalDateTime timestamp;
}
