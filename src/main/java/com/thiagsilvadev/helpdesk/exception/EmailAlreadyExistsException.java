package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "Email already exists: " + email);
    }
}
