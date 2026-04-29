package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.UserApi;
import com.thiagsilvadev.helpdesk.dto.user.ChangeUserRoleRequest;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserNameRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse createdUser = userService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(createdUser);
    }

    @Override
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id
    ) {
        UserResponse user = userService.getUserResponseById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        Page<UserResponse> users = userService.findAll(pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id,
            @RequestBody @Valid UpdateUserNameRequest request
    ) {
        UserResponse updatedUser = userService.update(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable @Min(value = 1, message = "id must be greater than 0") Long id,
            @RequestBody @Valid ChangeUserRoleRequest request
    ) {
        UserResponse updatedUser = userService.changeRole(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id
    ) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
