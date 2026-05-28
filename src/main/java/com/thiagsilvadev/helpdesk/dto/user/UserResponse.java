package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.user.Roles;

public record UserResponse(Long id, String name, String email, Roles role, boolean active) {}
