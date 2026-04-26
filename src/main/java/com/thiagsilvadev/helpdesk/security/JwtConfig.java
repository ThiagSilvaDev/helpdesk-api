package com.thiagsilvadev.helpdesk.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);
    private static final int MINIMUM_HS256_KEY_BYTES = 32;

    @Bean
    public SecretKey jwtSigningKey(@Value("${security.jwt.secret}") String secret) {
        byte[] keyBytes = resolveSecretBytes(secret);
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
    public JwtDecoder jwtDecoder(SecretKey jwtSigningKey,
                                 @Value("${security.jwt.issuer}") String issuer) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));

        return jwtDecoder;
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
