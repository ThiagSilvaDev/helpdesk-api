package com.thiagsilvadev.helpdesk.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CurrentUserIdResolverTest {

    private final CurrentUserIdResolver resolver = new CurrentUserIdResolver();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSupportLongParameterAnnotatedWithCurrentUserId() throws Exception {
        MethodParameter supported = parameter("supported", 0);
        MethodParameter unsupported = parameter("unsupported", 0);

        assertThat(resolver.supportsParameter(supported)).isTrue();
        assertThat(resolver.supportsParameter(unsupported)).isFalse();
    }

    @Test
    void shouldResolveCurrentUserIdFromJwtSubject() throws Exception {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("42")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(jwt, null));

        Object resolved = resolver.resolveArgument(parameter("supported", 0), null, null, null);

        assertThat(resolved).isEqualTo(42L);
    }

    @Test
    void shouldThrowWhenJwtIsMissingFromSecurityContext() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("principal", null));

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> resolver.resolveArgument(parameter("supported", 0), null, null, null))
                .withMessage("No JWT found in Security Context");
    }

    private MethodParameter parameter(String methodName, int index) throws Exception {
        Method method = TestEndpoint.class.getDeclaredMethod(methodName, Long.class);
        return new MethodParameter(method, index);
    }

    private static class TestEndpoint {
        void supported(@CurrentUserId Long userId) {
        }

        void unsupported(Long userId) {
        }
    }
}
