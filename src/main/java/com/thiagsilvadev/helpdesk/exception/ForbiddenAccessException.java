package com.thiagsilvadev.helpdesk.exception;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException(String message) {
        super(message);
    }

    public ForbiddenAccessException() {
        super("You do not have permission to access this resource");
    }
}
