package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.AuthDTO;
import com.thiagsilvadev.helpdesk.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthDTO.Response.class))
			),
			@ApiResponse(responseCode = "401", ref = "Unauthorized"),
			@ApiResponse(responseCode = "400", ref = "BadRequest")
	})
	public ResponseEntity<AuthDTO.Response> authenticateUserAndIssueToken(@RequestBody @Valid AuthDTO.Login.Request request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}
}
