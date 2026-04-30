package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Order(0)
public class WebExceptionHandler {

    private final ProblemDetailFactory problemDetails;

    public WebExceptionHandler(ProblemDetailFactory problemDetails) {
        this.problemDetails = problemDetails;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        if (ex.getSupportedMethods() != null) {
            List<String> supportedMethods = Arrays.asList(ex.getSupportedMethods());
            return problemDetails.enrich(ex.getBody(), Map.of("supported_methods", supportedMethods));
        }

        return problemDetails.enrich(ex.getBody());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        return problemDetails.enrich(ex.getBody());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        if (!ex.getSupportedMediaTypes().isEmpty()) {
            List<String> supportedMediaTypes = ex.getSupportedMediaTypes().stream()
                    .map(MediaType::toString)
                    .toList();
            return problemDetails.enrich(ex.getBody(), Map.of("supported_media_types", supportedMediaTypes));
        }

        return problemDetails.enrich(ex.getBody());
    }
}
