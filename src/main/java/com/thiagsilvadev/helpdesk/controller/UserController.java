package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
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

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable @Min(value = 1, message = "id must be greater than 0") Long id) {
        UserResponse user = userService.getUserResponseById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable @Min(value = 1, message = "id must be greater than 0") Long id, @RequestBody @Valid UpdateUserRequest request) {
        UserResponse updatedUser = userService.update(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
