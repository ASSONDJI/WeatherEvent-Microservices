package com.agenda.exception;

public abstract class AgendaException extends RuntimeException {
    private final String errorCode;
    protected AgendaException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    public String getErrorCode() { return errorCode; }
}
