package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.GlobalExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Order(1)
public class WebGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String[] methods = ex.getSupportedMethods();
        String message = methods == null || methods.length == 0
                ? "Supported methods information is unavailable"
                : "Supported methods: " + String.join(", ", methods);
        List<String> errors = List.of(message);
        return enrichProblemDetail(ex.getBody(), errors);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        String message = "Resource not found: " + ex.getMessage();
        List<String> errors = List.of(message);
        return enrichProblemDetail(ex.getBody(), errors);
    }

}
