package com.thiagsilvadev.helpdesk.exception.handler;

import com.thiagsilvadev.helpdesk.exception.ProblemDetailFactory;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    private final ProblemDetailFactory problemDetails;

    public SecurityExceptionHandler(ProblemDetailFactory problemDetails) {
        this.problemDetails = problemDetails;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.warn("Access denied for {} on {}",
                describePrincipal(auth), request.getRequestURI());

        return problemDetails.create(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(HttpServletRequest request) {
        logger.warn("Bad credentials attempt on URI: {}", request.getRequestURI());

        return problemDetails.create(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        logger.warn("Authentication failed on {} with {}",
                request.getRequestURI(), ex.getClass().getSimpleName());

        return problemDetails.create(HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.");
    }

    private String describePrincipal(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }

        if (auth.getPrincipal() instanceof UserPrincipal userPrincipal && userPrincipal.getId() != null) {
            return "userId=" + userPrincipal.getId();
        }

        if (auth.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
            return "userId=" + jwt.getSubject();
        }

        return "authenticated-user";
    }
}
