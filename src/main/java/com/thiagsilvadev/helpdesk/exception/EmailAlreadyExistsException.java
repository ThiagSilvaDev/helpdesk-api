package com.thiagsilvadev.helpdesk.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessRuleException {
    public EmailAlreadyExistsException(String email) {
        super(HttpStatus.CONFLICT, "The provided email address is already in use.", Map.of("email", email));
    }
}
