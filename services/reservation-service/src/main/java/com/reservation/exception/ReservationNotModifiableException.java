package com.reservation.exception;

public class ReservationNotModifiableException extends ReservationException {
    
    public ReservationNotModifiableException(String id, String currentStatus) {
        super(String.format(
            "Reservation '%s' cannot be modified because it is in status: %s. Only PENDING reservations can be modified.",
            id, currentStatus), "RESERVATION_NOT_MODIFIABLE");
    }
}
