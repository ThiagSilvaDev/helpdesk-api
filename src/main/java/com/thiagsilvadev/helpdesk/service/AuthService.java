package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.AuthDTO;
import com.thiagsilvadev.helpdesk.security.JwtService;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthDTO.Response authenticate(AuthDTO.Login.Request request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("User authenticated successfully with id={}", Objects.requireNonNull(userPrincipal).getId());
        String token = jwtService.generateToken(Objects.requireNonNull(userPrincipal));

        return new AuthDTO.Response(token);
    }
}
