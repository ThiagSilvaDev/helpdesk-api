package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "The provided email address is already in use.", Map.of("email", email));
    }
}
