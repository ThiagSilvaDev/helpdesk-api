package com.thiagsilvadev.helpdesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for the authentication endpoint.
 * Limits requests per IP to prevent brute-force attacks.
 *
 * <p>For production deployments behind a load balancer, consider using
 * a distributed rate limiter (e.g., Redis-backed via Spring Cloud Gateway).</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000L;

    private final ObjectMapper objectMapper;
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        RateLimitBucket bucket = buckets.compute(clientIp, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new RateLimitBucket();
            }
            return existing;
        });

        if (bucket.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            ProblemDetail problem = buildRateLimitProblem(request);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getOutputStream(), problem);
        }
    }

    private ProblemDetail buildRateLimitProblem(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded. Try again later."
        );
        problem.setType(URI.create(baseUrl + "/errors/too-many-requests"));
        problem.setInstance(URI.create(baseUrl + request.getRequestURI()));
        return problem;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long windowStart = System.currentTimeMillis();

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > WINDOW_MS;
        }

        boolean tryConsume() {
            return count.incrementAndGet() <= MAX_REQUESTS;
        }
    }
}
