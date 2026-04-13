package com.thiagsilvadev.helpdesk.exception;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public abstract class GlobalExceptionHandler {

    protected ProblemDetail enrichProblemDetail(ProblemDetail problemDetail, @Nullable List<InvalidParam> invalidParam) {
        int statusCode = problemDetail.getStatus();
        HttpStatus status = HttpStatus.resolve(statusCode);

        URI typeUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/errors/{slug}")
                .buildAndExpand(resolveStatusSlug(statusCode, status))
                .toUri();

        problemDetail.setType(typeUri);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            URI instanceUri = URI.create(attributes.getRequest().getRequestURI());
            problemDetail.setInstance(instanceUri);
        } else {
            problemDetail.setInstance(URI.create("urn:helpdesk:background-process"));
        }
        
        problemDetail.setProperty("timestamp", Instant.now());

        if (invalidParam != null) {
            problemDetail.setProperty("invalid_params", invalidParam);
        }

        return problemDetail;
    }
    
    protected ProblemDetail createProblemDetail(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        return enrichProblemDetail(detail, null);
    }

    private String resolveStatusSlug(int statusCode, HttpStatus status) {
        if (status == null) {
            return "status-" + statusCode;
        }
        return status.name().toLowerCase().replace('_', '-');
    }
}
