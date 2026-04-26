package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

public class InvalidTicketStateException extends BusinessRuleException {
    public InvalidTicketStateException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
