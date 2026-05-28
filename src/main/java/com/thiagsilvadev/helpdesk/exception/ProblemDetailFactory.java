package com.thiagsilvadev.helpdesk.exception;

import jakarta.annotation.Nullable;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class ProblemDetailFactory {

    private final Clock clock;

    public ProblemDetailFactory(Clock clock) {
        this.clock = clock;
    }

    public ProblemDetail create(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        return enrich(detail, null);
    }

    public ProblemDetail enrich(ProblemDetail problemDetail) {
        return enrich(problemDetail, null);
    }

    public ProblemDetail enrich(ProblemDetail problemDetail, @Nullable Map<String, Object> customProperties) {
        int statusCode = problemDetail.getStatus();
        HttpStatus status = HttpStatus.resolve(statusCode);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            URI typeUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/errors/{slug}")
                    .buildAndExpand(resolveStatusSlug(statusCode, status))
                    .toUri();
            URI instanceUri = URI.create(attributes.getRequest().getRequestURI());

            problemDetail.setType(typeUri);
            problemDetail.setInstance(instanceUri);
        } else {
            problemDetail.setType(URI.create("urn:helpdesk:error:" + resolveStatusSlug(statusCode, status)));
            problemDetail.setInstance(URI.create("urn:helpdesk:background-process"));
        }

        problemDetail.setProperty("timestamp", Instant.from(clock.instant()));

        if (customProperties != null) {
            customProperties.forEach(problemDetail::setProperty);
        }

        return problemDetail;
    }

    private String resolveStatusSlug(int statusCode, HttpStatus status) {
        if (status == null) {
            return "status-" + statusCode;
        }
        return status.name().toLowerCase().replace('_', '-');
    }
}
