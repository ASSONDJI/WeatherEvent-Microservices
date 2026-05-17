package com.reservation.exception;

public class ResourceNotFoundException extends ReservationException {
    
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id), "RESOURCE_NOT_FOUND");
    }
}
