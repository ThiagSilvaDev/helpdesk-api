package com.thiagsilvadev.helpdesk.security.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

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
        refreshTokenService = new RefreshTokenService(redisTemplate, new SecureRandom(), Duration.ofDays(7));
    }

    @Test
    void shouldIssueOpaqueRefreshToken() {
        RefreshTokenService.RefreshTokenIssue issue = refreshTokenService.issue(42L);

        assertThat(issue.token()).isNotBlank();
        assertThat(issue.expiresIn()).isEqualTo(Duration.ofDays(7).toSeconds());
        verify(hashOperations).putAll(startsWith("refresh:token:"), argThat(value ->
                value instanceof Map<?, ?> map
                        && map.containsValue("42")
                        && map.containsValue("ACTIVE")
        ));
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
        given(redisTemplate.execute(any(DefaultRedisScript.class), anyList())).willReturn(List.of("REUSED", "family-1"));

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
        verify(hashOperations).putAll(startsWith("refresh:token:"), argThat(value ->
                value instanceof Map<?, ?> map
                        && map.containsValue("42")
                        && map.containsValue("family-1")
                        && map.containsValue("ACTIVE")
        ));
    }
}
