package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.InvalidRoleAssignmentException;
import com.thiagsilvadev.helpdesk.exception.InvalidTicketStateException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(2)
public class DomainGlobalExceptionHandler {

    private final ProblemDetailFactory problemDetails;

    public DomainGlobalExceptionHandler(ProblemDetailFactory problemDetails) {
        this.problemDetails = problemDetails;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        return problemDetails.create(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problemDetails.create(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTicketStateException.class)
    public ProblemDetail handleInvalidTicketState(InvalidTicketStateException ex) {
        return problemDetails.create(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(InvalidRoleAssignmentException.class)
    public ProblemDetail handleInvalidRoleAssignment(InvalidRoleAssignmentException ex) {
        return problemDetails.create(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }
}
