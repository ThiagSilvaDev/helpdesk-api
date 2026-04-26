package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.config.JwtConfig;
import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JwtServiceTest {

    private static final String SECRET = "c3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc=";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String ISSUER = "helpdesk-api";
    private static final Long USER_ID = 42L;

    private final JwtConfig jwtConfig = new JwtConfig();
    private final SecretKey signingKey = jwtConfig.jwtSigningKey(SECRET);
    private final JwtEncoder jwtEncoder = jwtConfig.jwtEncoder(signingKey);
    private final JwtDecoder jwtDecoder = jwtConfig.jwtDecoder(signingKey, ISSUER);
    private final JwtService jwtService = new JwtService(jwtEncoder, EXPIRATION_MS, ISSUER);

    @Test
    void shouldGenerateTokenWithSubjectEmailRoleAndIssuer() {
        UserPrincipal userPrincipal = new UserPrincipal(user(USER_ID, Roles.ROLE_TECHNICIAN));

        String token = jwtService.generateToken(userPrincipal);
        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo(USER_ID.toString());
        assertThat(decoded.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(decoded.getClaimAsString("email")).isEqualTo("tech@helpdesk.local");
        assertThat(decoded.getClaimAsStringList("roles")).containsExactly(Roles.ROLE_TECHNICIAN.name());
        assertThat(decoded.getExpiresAt()).isAfter(decoded.getIssuedAt());
    }

    @Test
    void shouldRejectPrincipalWithoutId() {
        UserPrincipal userPrincipal = new UserPrincipal(user(null, Roles.ROLE_USER));

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> jwtService.generateToken(userPrincipal))
                .withMessage("user id must not be null");
    }

    private User user(Long id, Roles role) {
        User user = new User(
                role == Roles.ROLE_TECHNICIAN ? "Tech User" : "Jane User",
                role == Roles.ROLE_TECHNICIAN ? "tech@helpdesk.local" : "jane@helpdesk.local",
                "encoded-password",
                role
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
