package com.thiagsilvadev.helpdesk.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thiagsilvadev.helpdesk.infrastructure.IdGenerator;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-09T18:00:00Z");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private StringRedisTemplate redisTemplate;
    private HashOperations<String, Object, Object> hashOperations;
    private ValueOperations<String, String> valueOperations;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        hashOperations = mock(HashOperations.class);
        valueOperations = mock(ValueOperations.class);
        given(redisTemplate.opsForHash()).willReturn(hashOperations);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        IdGenerator idGenerator = () -> FAMILY_ID;
        refreshTokenService =
                new RefreshTokenService(redisTemplate, new SecureRandom(), clock, idGenerator, Duration.ofDays(7));
    }

    @Test
    void shouldIssueOpaqueRefreshToken() {
        RefreshTokenService.RefreshTokenIssue issue = refreshTokenService.issue(42L);

        assertThat(issue.token()).isNotBlank();
        assertThat(issue.expiresIn()).isEqualTo(Duration.ofDays(7).toSeconds());
        verify(hashOperations)
                .putAll(
                        startsWith("refresh:token:"),
                        argThat(value -> value instanceof Map<?, ?> map
                                && map.containsValue("42")
                                && map.containsValue(FAMILY_ID.toString())
                                && map.containsValue("ACTIVE")
                                && map.containsValue(
                                        NOW.plus(Duration.ofDays(7)).toString())));
        verify(redisTemplate).expire(startsWith("refresh:token:"), eq(Duration.ofDays(7)));
    }

    @Test
    void shouldRejectMissingToken() {
        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> refreshTokenService.rotate(" "))
                .withMessage("Refresh token is required");
    }

    @Test
    void shouldRejectUnknownToken() {
        given(redisTemplate.execute(any(DefaultRedisScript.class), anyList())).willReturn(List.of("NOT_FOUND"));

        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> refreshTokenService.rotate("unknown-token"))
                .withMessage("Refresh token is invalid or expired");
    }

    @Test
    void shouldRevokeFamilyOnRefreshTokenReuse() {
        given(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                .willReturn(List.of("REUSED", "family-1"));

        assertThatExceptionOfType(InvalidRefreshTokenException.class)
                .isThrownBy(() -> refreshTokenService.rotate("used-token"))
                .withMessage("Refresh token reuse detected. Session terminated.");

        verify(valueOperations).set("refresh:family:family-1", "REVOKED", Duration.ofDays(7));
    }

    @Test
    void shouldRotateRefreshTokenAtomically() {
        given(redisTemplate.execute(any(DefaultRedisScript.class), anyList()))
                .willReturn(List.of("OK", "42", "family-1"));

        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotate("active-token");

        assertThat(rotation.userId()).isEqualTo(42L);
        assertThat(rotation.refreshToken()).isNotBlank();
        assertThat(rotation.refreshExpiresIn()).isEqualTo(Duration.ofDays(7).toSeconds());
        verify(valueOperations).get("refresh:family:family-1");
        verify(hashOperations)
                .putAll(
                        startsWith("refresh:token:"),
                        argThat(value -> value instanceof Map<?, ?> map
                                && map.containsValue("42")
                                && map.containsValue("family-1")
                                && map.containsValue("ACTIVE")));
    }
}
