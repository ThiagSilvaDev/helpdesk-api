package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.user.ChangeUserRoleRequest;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserNameRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
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

@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User management (admin scope)")
@SecurityRequirement(name = "bearerAuth")
@ApiSecurityResponseErrors
public interface UserApi {

    @PostMapping
    @Operation(operationId = "createUser")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User created"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request);

    @GetMapping("/{id}")
    @Operation(operationId = "getUserById")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User found"
            ),
            @ApiResponse(responseCode = "400", ref = "Bad Request"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<UserResponse> getUserById(
            @PathVariable @Min(value = 1) Long id
    );

    @GetMapping
    @Operation(operationId = "listUsers")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved"
            )
    })
    ResponseEntity<Page<UserResponse>> listUsers(@ParameterObject Pageable pageable);

    @PatchMapping("/{id}/name")
    @Operation(operationId = "updateUserName")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<UserResponse> updateUser(
            @PathVariable @Min(value = 1) Long id,
            @RequestBody @Valid UpdateUserNameRequest request
    );

    @PatchMapping("/{id}/role")
    @Operation(operationId = "changeUserRole")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User role changed"
            ),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<UserResponse> changeUserRole(
            @PathVariable @Min(value = 1) Long id,
            @RequestBody @Valid ChangeUserRoleRequest request
    );

    @DeleteMapping("/{id}")
    @Operation(operationId = "deactivateUser")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    ResponseEntity<Void> deactivateUser(
            @PathVariable @Min(value = 1) Long id
    );
}
