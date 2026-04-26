package com.thiagsilvadev.helpdesk.exception;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@Component
public class ProblemDetailFactory {

    public ProblemDetail create(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        return enrich(detail, null);
    }

    public ProblemDetail enrich(ProblemDetail problemDetail, @Nullable List<InvalidParam> invalidParams) {
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

        if (invalidParams != null) {
            problemDetail.setProperty("invalid_params", invalidParams);
        }

        return problemDetail;
    }

    private String resolveStatusSlug(int statusCode, HttpStatus status) {
        if (status == null) {
            return "status-" + statusCode;
        }
        return status.name().toLowerCase().replace('_', '-');
    }

    public record InvalidParam(String name, String reason) {
    }
}
