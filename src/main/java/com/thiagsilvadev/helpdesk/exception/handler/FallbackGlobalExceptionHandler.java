package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order()
public class FallbackGlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FallbackGlobalExceptionHandler.class);

    private final ProblemDetailFactory problemDetails;

    public FallbackGlobalExceptionHandler(ProblemDetailFactory problemDetails) {
        this.problemDetails = problemDetails;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        logger.error("Unhandled exception reached fallback handler", ex);
        return problemDetails.create(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred");
    }
}
