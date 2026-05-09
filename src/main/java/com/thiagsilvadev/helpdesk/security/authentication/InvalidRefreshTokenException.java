package com.thiagsilvadev.helpdesk.security.authentication;

import org.springframework.security.core.AuthenticationException;

public class InvalidRefreshTokenException extends AuthenticationException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
