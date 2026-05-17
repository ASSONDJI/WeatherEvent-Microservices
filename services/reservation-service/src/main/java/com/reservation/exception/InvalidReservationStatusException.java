package com.reservation.exception;

public class InvalidReservationStatusException extends ReservationException {
    
    public InvalidReservationStatusException(String currentStatus, String targetStatus) {
        super(String.format(
            "Cannot transition from status '%s' to '%s'",
            currentStatus, targetStatus), "INVALID_STATUS_TRANSITION");
    }
}
