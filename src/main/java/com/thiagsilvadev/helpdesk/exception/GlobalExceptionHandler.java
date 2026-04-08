package com.thiagsilvadev.helpdesk.exception;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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

        URI instanceUri = URI.create(Objects.requireNonNull(
                ServletUriComponentsBuilder.fromCurrentRequest().build().getPath()
        ));

        problemDetail.setInstance(instanceUri);
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
