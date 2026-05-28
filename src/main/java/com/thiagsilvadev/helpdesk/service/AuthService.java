package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LogoutRequest;
import com.thiagsilvadev.helpdesk.dto.auth.RefreshTokenRequest;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.security.authentication.JwtService;
import com.thiagsilvadev.helpdesk.security.authentication.RefreshTokenService;
import com.thiagsilvadev.helpdesk.security.authentication.UserPrincipal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    public AuthResponse authenticate(AuthLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = Objects.requireNonNull(userPrincipal).getId();
        log.atInfo()
                .setMessage("Authentication successful")
                .addKeyValue("userId", userId)
                .log();
        String token = jwtService.generateToken(Objects.requireNonNull(userPrincipal));
        RefreshTokenService.RefreshTokenIssue refreshToken = refreshTokenService.issue(userId);

        return AuthResponse.bearer(
                token,
                refreshToken.token(),
                jwtService.getExpirationSeconds(),
                refreshToken.expiresIn(),
                toAuthUserResponse(userPrincipal));
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RefreshTokenRotation rotation = refreshTokenService.rotate(request.refreshToken());
        User user = userService.getUserById(rotation.userId());
        if (!user.isActive()) {
            refreshTokenService.revoke(request.refreshToken());
            throw new DisabledException("User account is disabled");
        }
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);

        return AuthResponse.refresh(
                token, rotation.refreshToken(), jwtService.getExpirationSeconds(), rotation.refreshExpiresIn());
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    public AuthenticatedUserResponse getAuthenticatedUser(Long userId) {
        User user = userService.getUserById(userId);
        return new AuthenticatedUserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), user.isActive());
    }

    private AuthUserResponse toAuthUserResponse(UserPrincipal userPrincipal) {
        return new AuthUserResponse(userPrincipal.getId(), userPrincipal.getName(), userPrincipal.getRole());
    }

    private AuthUserResponse toAuthUserResponse(User user) {
        return new AuthUserResponse(user.getId(), user.getName(), user.getRole());
    }
}
