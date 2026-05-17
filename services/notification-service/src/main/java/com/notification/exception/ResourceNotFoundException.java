package com.notification.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id));
    }

    public String getErrorCode() { return errorCode; }
}
