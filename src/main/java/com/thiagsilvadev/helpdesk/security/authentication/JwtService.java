package com.thiagsilvadev.helpdesk.security.authentication;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_USER_ROLES = "roles";
    private static final String CLAIM_TOKEN_USE = "token_use";
    private static final String TOKEN_USE_ACCESS = "access";

    private final JwtEncoder jwtEncoder;
    private final long expirationMs;
    private final String issuer;
    private final String audience;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${security.jwt.expiration-ms}") long expirationMs,
                      @Value("${security.jwt.issuer}") String issuer,
                      @Value("${security.jwt.audience}") String audience) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(expirationMs);
        Long userId = Objects.requireNonNull(userPrincipal.getId(), "user id must not be null");
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("user must have at least one authority");
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .audience(List.of(audience))
                .issuedAt(now)
                .notBefore(now)
                .expiresAt(expiryDate)
                .claim(CLAIM_TOKEN_USE, TOKEN_USE_ACCESS)
                .claim(CLAIM_USER_ROLES, roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
