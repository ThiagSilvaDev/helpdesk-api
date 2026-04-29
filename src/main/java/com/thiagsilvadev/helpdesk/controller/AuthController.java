package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	@Operation(
			operationId = "authenticateUserAndIssueToken",
			summary = "Authenticate user",
			description = "Authenticates with email/password and returns a JWT token"
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Authentication successful"
			),
			@ApiResponse(responseCode = "401", ref = "Unauthorized"),
			@ApiResponse(responseCode = "400", ref = "BadRequest")
	})
	public ResponseEntity<AuthResponse> authenticateUserAndIssueToken(@RequestBody @Valid AuthLoginRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

	@GetMapping("/me")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(
			operationId = "getAuthenticatedUser",
			summary = "Get authenticated user",
			description = "Returns the profile for the user identified by the current JWT subject"
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated user retrieved"),
			@ApiResponse(responseCode = "401", ref = "Unauthorized"),
			@ApiResponse(responseCode = "404", ref = "NotFound")
	})
	public ResponseEntity<AuthenticatedUserResponse> getAuthenticatedUser(
			@Parameter(hidden = true) @CurrentUserId Long userId
	) {
		return ResponseEntity.ok(authService.getAuthenticatedUser(userId));
	}
}
