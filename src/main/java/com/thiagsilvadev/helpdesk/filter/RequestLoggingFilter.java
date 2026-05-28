package com.thiagsilvadev.helpdesk.filter;

import com.thiagsilvadev.helpdesk.infrastructure.IdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    private final IdGenerator idGenerator;

    public RequestLoggingFilter(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health") || path.startsWith("/actuator/prometheus");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = extractOrGenerateRequestId(request.getHeader(REQUEST_ID_HEADER));
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startedAt = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequestCompletion(request, response, startedAt);
            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    private String extractOrGenerateRequestId(String requestIdHeader) {
        if (StringUtils.hasText(requestIdHeader)) {
            return requestIdHeader;
        }
        return idGenerator.nextUuidString();
    }

    private void logRequestCompletion(HttpServletRequest request, HttpServletResponse response, long startedAt) {
        long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
        int status = response.getStatus();

        if (status >= HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            log.error("HTTP {} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
            return;
        }

        log.info("HTTP {} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), status, elapsedMs);
    }
}
