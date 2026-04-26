package com.thiagsilvadev.helpdesk.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class JwtService {

    private static final String CLAIM_USER_EMAIL = "email";
    private static final String CLAIM_USER_ROLES = "roles";

    private final JwtEncoder jwtEncoder;
    private final long expirationMs;
    private final String issuer;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${security.jwt.expiration-ms}") long expirationMs,
                      @Value("${security.jwt.issuer}") String issuer) {
        this.jwtEncoder = jwtEncoder;
        this.expirationMs = expirationMs;
        this.issuer = issuer;
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
                .issuedAt(now)
                .expiresAt(expiryDate)
                .claim(CLAIM_USER_EMAIL, userPrincipal.getUsername())
                .claim(CLAIM_USER_ROLES, roles)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
