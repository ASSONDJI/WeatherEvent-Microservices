package com.agenda.exception;

public class ExternalServiceException extends AgendaException {
    public ExternalServiceException(String service, String reason) {
        super(String.format("External service '%s' is unavailable: %s", service, reason),
              "EXTERNAL_SERVICE_ERROR");
    }
}
