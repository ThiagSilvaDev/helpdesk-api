package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.UserDTO;
import com.thiagsilvadev.helpdesk.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management (admin scope)")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "409", ref = "Conflict")
    })
    public ResponseEntity<UserDTO.Response> createUser(@RequestBody @Valid UserDTO.Create.Request request) {
        UserDTO.Response createdUser = userService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a single user by ID (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.Response.class))),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public ResponseEntity<UserDTO.Response> getUserById(@PathVariable @Min(value = 1, message = "id must be greater than 0") Long id) {
        UserDTO.Response user = userService.getUserResponseById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all users", description = "Returns a paginated list of all users (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public ResponseEntity<Page<UserDTO.Response>> listUsers(
            @ParameterObject Pageable pageable) {
        Page<UserDTO.Response> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{id}/name")
    @Operation(summary = "Update user name", description = "Updates the user's name (admin or self)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<UserDTO.Response> updateUser(@PathVariable @Min(value = 1, message = "id must be greater than 0") Long id, @RequestBody @Valid UserDTO.Update.Request request) {
        UserDTO.Response updatedUser = userService.update(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user role", description = "Changes a user's role (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User role changed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<UserDTO.Response> changeUserRole(@PathVariable @Min(value = 1, message = "id must be greater than 0") Long id, @RequestBody @Valid UserDTO.ChangeRole.Request request) {
        UserDTO.Response updatedUser = userService.changeRole(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate user", description = "Soft-deletes a user by setting active=false (admin only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deactivated"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
