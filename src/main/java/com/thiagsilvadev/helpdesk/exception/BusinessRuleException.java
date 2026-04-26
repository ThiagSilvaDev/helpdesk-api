package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

public abstract class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;

    protected BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
