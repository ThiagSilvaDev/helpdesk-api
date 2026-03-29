package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LoginRequest;
import com.thiagsilvadev.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}
}
