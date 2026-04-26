package com.thiagsilvadev.helpdesk.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoleAssignmentException extends BusinessRuleException {
    public InvalidRoleAssignmentException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}
