package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.BusinessRuleException;
import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import org.springframework.core.annotation.Order;
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

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problemDetails.create(ex.getStatus(), ex.getMessage());
    }
}
