package com.thiagsilvadev.helpdesk.security.authentication;

import com.thiagsilvadev.helpdesk.config.security.JwtProperties;
import com.thiagsilvadev.helpdesk.infrastructure.IdGenerator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private static final String TOKEN_KEY_PREFIX = "refresh:token:";
    private static final String FAMILY_KEY_PREFIX = "refresh:family:";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_FAMILY_ID = "familyId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_EXPIRES_AT = "expiresAt";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_REVOKED = "REVOKED";
    private static final int REFRESH_TOKEN_BYTES = 32;
    private static final String ROTATE_LUA_SCRIPT =
            """
            local data = redis.call('HMGET', KEYS[1], 'status', 'userId', 'familyId')
            local status = data[1]

            if not status then
                return {'NOT_FOUND'}
            end

            if status == 'USED' then
                return {'REUSED', data[3]}
            end

            if status == 'ACTIVE' then
                redis.call('HSET', KEYS[1], 'status', 'USED')
                return {'OK', data[2], data[3]}
            end

            return {'INVALID'}
            """;

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final IdGenerator idGenerator;
    private final Duration refreshTokenTtl;
    private final DefaultRedisScript<List> rotateScript;

    @Autowired
    public RefreshTokenService(
            StringRedisTemplate redisTemplate,
            SecureRandom secureRandom,
            Clock clock,
            IdGenerator idGenerator,
            JwtProperties jwtProperties) {
        this(redisTemplate, secureRandom, clock, idGenerator, Duration.ofMillis(jwtProperties.refreshExpirationMs()));
    }

    RefreshTokenService(
            StringRedisTemplate redisTemplate,
            SecureRandom secureRandom,
            Clock clock,
            IdGenerator idGenerator,
            Duration refreshTokenTtl) {
        this.redisTemplate = redisTemplate;
        this.secureRandom = secureRandom;
        this.clock = clock;
        this.idGenerator = idGenerator;
        this.refreshTokenTtl = refreshTokenTtl;
        this.rotateScript = new DefaultRedisScript<>(ROTATE_LUA_SCRIPT, List.class);
    }

    public RefreshTokenIssue issue(Long userId) {
        return issue(userId, idGenerator.nextUuidString());
    }

    public RefreshTokenRotation rotate(String refreshToken) {
        String tokenHash = hash(refreshToken);

        List<String> result = redisTemplate.execute(rotateScript, Collections.singletonList(tokenKey(tokenHash)));

        if (result == null || result.isEmpty()) {
            throw new IllegalStateException("System error during token rotation");
        }

        String code = result.getFirst();
        if ("NOT_FOUND".equals(code) || "INVALID".equals(code)) {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired");
        }
        if ("REUSED".equals(code)) {
            String familyId = result.get(1);
            revokeFamily(familyId);
            throw new InvalidRefreshTokenException("Refresh token reuse detected. Session terminated.");
        }
        if (!"OK".equals(code)) {
            throw new IllegalStateException("Unexpected token rotation result: " + code);
        }

        String userId = result.get(1);
        String familyId = result.get(2);
        if (isFamilyRevoked(familyId)) {
            throw new InvalidRefreshTokenException("Refresh token family has been revoked");
        }

        Long parsedUserId = Long.valueOf(userId);
        RefreshTokenIssue nextToken = issue(parsedUserId, familyId);
        return new RefreshTokenRotation(parsedUserId, nextToken.token(), getRefreshTokenTtlSeconds());
    }

    public void revoke(String refreshToken) {
        String tokenHash = hash(refreshToken);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(tokenKey(tokenHash));
        if (entries.isEmpty()) {
            return;
        }

        Object familyId = entries.get(FIELD_FAMILY_ID);
        if (familyId != null) {
            revokeFamily(familyId.toString());
        }
        redisTemplate.opsForHash().put(tokenKey(tokenHash), FIELD_STATUS, STATUS_REVOKED);
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtl.toSeconds();
    }

    private RefreshTokenIssue issue(Long userId, String familyId) {
        Objects.requireNonNull(userId, "user id must not be null");
        String token = generateOpaqueToken();
        String tokenHash = hash(token);
        Instant expiresAt = clock.instant().plus(refreshTokenTtl);

        redisTemplate
                .opsForHash()
                .putAll(
                        tokenKey(tokenHash),
                        Map.of(
                                FIELD_USER_ID,
                                userId.toString(),
                                FIELD_FAMILY_ID,
                                familyId,
                                FIELD_STATUS,
                                STATUS_ACTIVE,
                                FIELD_EXPIRES_AT,
                                expiresAt.toString()));
        redisTemplate.expire(tokenKey(tokenHash), refreshTokenTtl);
        return new RefreshTokenIssue(token, getRefreshTokenTtlSeconds());
    }

    private void revokeFamily(String familyId) {
        redisTemplate.opsForValue().set(familyKey(familyId), STATUS_REVOKED, refreshTokenTtl);
    }

    private boolean isFamilyRevoked(String familyId) {
        return STATUS_REVOKED.equals(redisTemplate.opsForValue().get(familyKey(familyId)));
    }

    private String generateOpaqueToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is required");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }

    private String tokenKey(String tokenHash) {
        return TOKEN_KEY_PREFIX + tokenHash;
    }

    private String familyKey(String familyId) {
        return FAMILY_KEY_PREFIX + familyId;
    }

    public record RefreshTokenIssue(String token, long expiresIn) {}

    public record RefreshTokenRotation(Long userId, String refreshToken, long refreshExpiresIn) {}
}
