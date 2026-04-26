package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BusinessRuleException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
