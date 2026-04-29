package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and token management")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(
            operationId = "authenticateUserAndIssueToken",
            summary = "Authenticate user",
            description = "Authenticates with email/password and returns a JWT token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "400", ref = "BadRequest")
    })
    ResponseEntity<AuthResponse> authenticateUserAndIssueToken(@RequestBody @Valid AuthLoginRequest request);

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            operationId = "getAuthenticatedUser",
            summary = "Get authenticated user",
            description = "Returns the profile for the user identified by the current JWT subject"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authenticated user retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthenticatedUserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<AuthenticatedUserResponse> getAuthenticatedUser(
            @Parameter(hidden = true) @CurrentUserId Long userId
    );
}
