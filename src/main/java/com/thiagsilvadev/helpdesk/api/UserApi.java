package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.user.ChangeUserRoleRequest;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserNameRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management (admin scope)")
@SecurityRequirement(name = "bearerAuth")
public interface UserApi {

    @PostMapping
    @Operation(operationId = "createUser", summary = "Create user", description = "Creates a new user (admin only)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getUserById", summary = "Get user by ID", description = "Returns a single user by ID (admin/technician)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User id", example = "42")
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id
    );

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "listUsers", summary = "List all users", description = "Returns a paginated list of all users (admin only)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    ResponseEntity<Page<UserResponse>> listUsers(@ParameterObject Pageable pageable);

    @PatchMapping("/{id}/name")
    @Operation(operationId = "updateUserName", summary = "Update user name", description = "Updates the user's name (admin or self)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User id", example = "42")
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id,
            @RequestBody @Valid UpdateUserNameRequest request
    );

    @PatchMapping("/{id}/role")
    @Operation(operationId = "changeUserRole", summary = "Change user role", description = "Changes a user's role (admin only)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User role changed",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<UserResponse> changeUserRole(
            @Parameter(description = "User id", example = "42")
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id,
            @RequestBody @Valid ChangeUserRoleRequest request
    );

    @DeleteMapping("/{id}")
    @Operation(operationId = "deactivateUser", summary = "Deactivate user", description = "Soft-deletes a user by setting active=false (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<Void> deactivateUser(
            @Parameter(description = "User id", example = "42")
            @PathVariable Long id
    );
}
