package com.thiagsilvadev.helpdesk.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidProperties() {
        JwtProperties properties = new JwtProperties(
                "test-secret-test-secret-test-secret-12345", 3_600_000L, 604_800_000L, "helpdesk-api", "helpdesk-api");

        assertThat(validator.validate(properties)).isEmpty();
    }

    @Test
    void shouldRejectInvalidProperties() {
        JwtProperties properties = new JwtProperties(" ", 0L, -1L, "", " ");

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("secret", "expirationMs", "refreshExpirationMs", "issuer", "audience");
    }
}
