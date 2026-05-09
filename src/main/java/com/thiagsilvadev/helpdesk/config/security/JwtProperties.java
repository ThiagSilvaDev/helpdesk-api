package com.thiagsilvadev.helpdesk.config.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        @NotBlank
        String secret,

        @Positive
        long expirationMs,

        @Positive
        long refreshExpirationMs,

        @NotBlank
        String issuer,

        @NotBlank
        String audience
) {
}
