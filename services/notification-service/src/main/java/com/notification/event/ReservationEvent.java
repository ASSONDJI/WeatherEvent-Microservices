package com.notification.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
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
