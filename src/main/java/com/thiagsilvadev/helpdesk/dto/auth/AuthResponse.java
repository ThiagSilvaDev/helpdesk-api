package com.thiagsilvadev.helpdesk.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        long refreshExpiresIn,
        AuthUserResponse user) {
    public AuthResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }

    public static AuthResponse bearer(
            String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn, AuthUserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn, user);
    }

    public static AuthResponse refresh(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn, null);
    }
}
