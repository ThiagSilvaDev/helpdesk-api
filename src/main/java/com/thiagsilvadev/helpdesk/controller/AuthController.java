package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.AuthApi;
import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public ResponseEntity<AuthResponse> authenticateUserAndIssueToken(@RequestBody @Valid AuthLoginRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

	@Override
	public ResponseEntity<AuthenticatedUserResponse> getAuthenticatedUser(
			@CurrentUserId Long userId
	) {
		return ResponseEntity.ok(authService.getAuthenticatedUser(userId));
	}
}
