package com.thiagsilvadev.helpdesk.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.thiagsilvadev.helpdesk.config.security.JwtConfig;
import com.thiagsilvadev.helpdesk.config.security.JwtProperties;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.infrastructure.IdGenerator;
import com.thiagsilvadev.helpdesk.security.authentication.JwtClaims;
import com.thiagsilvadev.helpdesk.security.authentication.JwtService;
import com.thiagsilvadev.helpdesk.security.authentication.UserPrincipal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = "c3VwZXItc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc=";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String ISSUER = "helpdesk-api";
    private static final String AUDIENCE = "helpdesk-api";
    private static final Long USER_ID = 42L;
    private static final Instant TOKEN_ISSUED_AT = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private static final UUID TOKEN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final JwtProperties JWT_PROPERTIES =
            new JwtProperties(SECRET, EXPIRATION_MS, 604_800_000L, ISSUER, AUDIENCE);

    private final JwtConfig jwtConfig = new JwtConfig();
    private final SecretKey signingKey = jwtConfig.jwtSigningKey(JWT_PROPERTIES);
    private final JwtEncoder jwtEncoder = jwtConfig.jwtEncoder(signingKey);
    private final JwtDecoder jwtDecoder = jwtConfig.jwtDecoder(signingKey, JWT_PROPERTIES);
    private final Clock clock = Clock.fixed(TOKEN_ISSUED_AT, ZoneOffset.UTC);
    private final IdGenerator idGenerator = () -> TOKEN_ID;
    private final JwtService jwtService = new JwtService(jwtEncoder, clock, idGenerator, JWT_PROPERTIES);

    @Test
    void shouldGenerateTokenWithSubjectRoleAndIssuer() {
        UserPrincipal userPrincipal = new UserPrincipal(user(USER_ID, Roles.ROLE_TECHNICIAN));

        String token = jwtService.generateToken(userPrincipal);
        Jwt decoded = jwtDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo(USER_ID.toString());
        assertThat(decoded.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(decoded.getAudience()).containsExactly(AUDIENCE);
        assertThat(decoded.getId()).isEqualTo(TOKEN_ID.toString());
        assertThat(decoded.getClaimAsString(JwtClaims.TOKEN_USE)).isEqualTo(JwtClaims.TOKEN_USE_ACCESS);
        assertThat(decoded.hasClaim("email")).isFalse();
        assertThat(decoded.getClaimAsStringList(JwtClaims.USER_ROLES)).containsExactly(Roles.ROLE_TECHNICIAN.name());
        assertThat(decoded.getIssuedAt()).isEqualTo(TOKEN_ISSUED_AT);
        assertThat(decoded.getNotBefore()).isEqualTo(TOKEN_ISSUED_AT);
        assertThat(decoded.getExpiresAt()).isEqualTo(TOKEN_ISSUED_AT.plusMillis(EXPIRATION_MS));
    }

    @Test
    void shouldRejectTokenWithWrongTokenUse() {
        String token = token("42", List.of(AUDIENCE), "refresh", List.of(Roles.ROLE_USER.name()));

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT token_use must be access");
    }

    @Test
    void shouldRejectTokenWithoutNumericSubject() {
        String token =
                token("not-a-user-id", List.of(AUDIENCE), JwtClaims.TOKEN_USE_ACCESS, List.of(Roles.ROLE_USER.name()));

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT subject must be a user id");
    }

    @Test
    void shouldRejectTokenWithoutSubject() {
        String token = token(null, List.of(AUDIENCE), JwtClaims.TOKEN_USE_ACCESS, List.of(Roles.ROLE_USER.name()));

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT subject must be a user id");
    }

    @Test
    void shouldRejectTokenWithEmptyRoles() {
        String token = token("42", List.of(AUDIENCE), JwtClaims.TOKEN_USE_ACCESS, List.of());

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT roles must not be empty");
    }

    @Test
    void shouldRejectTokenWithoutRoles() {
        String token = token("42", List.of(AUDIENCE), JwtClaims.TOKEN_USE_ACCESS, null);

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT roles must not be empty");
    }

    @Test
    void shouldRejectTokenWithWrongAudience() {
        String token = token("42", List.of("other-api"), JwtClaims.TOKEN_USE_ACCESS, List.of(Roles.ROLE_USER.name()));

        assertThatExceptionOfType(JwtException.class)
                .isThrownBy(() -> jwtDecoder.decode(token))
                .withMessageContaining("JWT audience is invalid");
    }

    @Test
    void shouldRejectPrincipalWithoutId() {
        UserPrincipal userPrincipal = new UserPrincipal(user(null, Roles.ROLE_USER));

        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> jwtService.generateToken(userPrincipal))
                .withMessage("user id must not be null");
    }

    private String token(String subject, List<String> audience, String tokenUse, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(audience)
                .issuedAt(now)
                .expiresAt(now.plusMillis(EXPIRATION_MS))
                .claim(JwtClaims.TOKEN_USE, tokenUse);

        if (subject != null) {
            claims.subject(subject);
        }
        if (roles != null) {
            claims.claim(JwtClaims.USER_ROLES, roles);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims.build()))
                .getTokenValue();
    }

    private User user(Long id, Roles role) {
        User user = new User(
                role == Roles.ROLE_TECHNICIAN ? "Tech User" : "Jane User",
                role == Roles.ROLE_TECHNICIAN ? "tech@helpdesk.local" : "jane@helpdesk.local",
                "encoded-password",
                role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
