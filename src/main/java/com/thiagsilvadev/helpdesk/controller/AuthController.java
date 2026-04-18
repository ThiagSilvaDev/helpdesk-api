package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LoginRequest;
import com.thiagsilvadev.helpdesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Operation(summary = "Authenticate user", description = "Authenticates with email/password and returns a JWT token")
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Authentication successful",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
			),
			@ApiResponse(
					responseCode = "401",
					description = "Invalid credentials",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Validation error",
					content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))
			)
	})
	public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}
}
