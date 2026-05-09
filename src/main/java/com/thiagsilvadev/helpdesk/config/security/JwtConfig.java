package com.thiagsilvadev.helpdesk.config.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.thiagsilvadev.helpdesk.security.authentication.JwtClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
    private static final int MINIMUM_HS256_KEY_BYTES = 32;

    @Bean
    public SecretKey jwtSigningKey(JwtProperties jwtProperties) {
        byte[] keyBytes = resolveSecretBytes(jwtProperties.secret());
        if (keyBytes.length < MINIMUM_HS256_KEY_BYTES) {
            throw new IllegalArgumentException("security.jwt.secret must be at least 256 bits for HS256 JWT signing");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey, JwtProperties jwtProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()),
                audienceValidator(jwtProperties.audience()),
                accessTokenUseValidator(),
                subjectValidator(),
                rolesValidator()
        ));

        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        return jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                "invalid_token",
                "JWT audience is invalid",
                null
        ));
    }

    private OAuth2TokenValidator<Jwt> accessTokenUseValidator() {
        return jwt -> {
            String tokenUse = jwt.getClaimAsString(JwtClaims.TOKEN_USE);
            return Objects.equals(tokenUse, JwtClaims.TOKEN_USE_ACCESS)
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "JWT token_use must be access",
                    null
            ));
        };
    }

    private OAuth2TokenValidator<Jwt> subjectValidator() {
        return jwt -> {
            String subject = jwt.getSubject();
            if (subject == null || subject.isBlank()) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token",
                        "JWT subject must be a user id",
                        null
                ));
            }

            try {
                Long.valueOf(subject);
                return OAuth2TokenValidatorResult.success();
            } catch (NumberFormatException ex) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                        "invalid_token",
                        "JWT subject must be a user id",
                        null
                ));
            }
        };
    }

    private OAuth2TokenValidator<Jwt> rolesValidator() {
        return jwt -> {
            List<String> roles = jwt.getClaimAsStringList(JwtClaims.USER_ROLES);
            return roles != null && !roles.isEmpty()
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "JWT roles must not be empty",
                    null
            ));
        };
    }

    private byte[] resolveSecretBytes(String secret) {
        try {
            byte[] decoded = Base64.getDecoder().decode(secret);
            if (decoded.length >= MINIMUM_HS256_KEY_BYTES) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
        }

        logger.warn("JWT secret is not valid Base64 or decodes to less than 256 bits; falling back to plain text encoding. "
                + "Use a Base64-encoded secret in production environments.");
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
