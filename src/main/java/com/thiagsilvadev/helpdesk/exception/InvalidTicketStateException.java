package com.thiagsilvadev.helpdesk.exception;

public class InvalidTicketStateException extends RuntimeException {
    public InvalidTicketStateException(String message) {
        super(message);
    }
}
