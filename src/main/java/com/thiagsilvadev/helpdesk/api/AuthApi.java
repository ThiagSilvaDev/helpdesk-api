package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LogoutRequest;
import com.thiagsilvadev.helpdesk.dto.auth.RefreshTokenRequest;
import com.thiagsilvadev.helpdesk.security.web.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Login and token management")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(operationId = "authenticateUserAndIssueToken")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", ref = "Unauthorized"),
        @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<AuthResponse> authenticateUserAndIssueToken(@RequestBody @Valid AuthLoginRequest request);

    @PostMapping("/refresh")
    @Operation(operationId = "refreshAuthenticationTokens")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tokens refreshed"),
        @ApiResponse(responseCode = "401", ref = "Unauthorized"),
        @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<AuthResponse> refreshAuthenticationTokens(@RequestBody @Valid RefreshTokenRequest request);

    @PostMapping("/logout")
    @Operation(operationId = "logoutAuthenticatedSession")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Refresh token revoked"),
        @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<Void> logoutAuthenticatedSession(@RequestBody @Valid LogoutRequest request);

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(operationId = "getAuthenticatedUser")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated user retrieved"),
        @ApiResponse(responseCode = "401", ref = "Unauthorized")
    })
    ResponseEntity<AuthenticatedUserResponse> getAuthenticatedUser(@CurrentUserId Long userId);
}
