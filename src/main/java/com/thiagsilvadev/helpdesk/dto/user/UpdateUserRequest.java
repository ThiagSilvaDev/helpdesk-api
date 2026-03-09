package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;

public record UpdateUserRequest(
        String name,
        String email,
        Roles role
) {
}

