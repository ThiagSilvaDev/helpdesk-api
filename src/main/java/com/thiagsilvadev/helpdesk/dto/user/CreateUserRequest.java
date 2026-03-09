package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;

public record CreateUserRequest(
        String name,
        String email,
        Roles role
) {
    public User toEntity() {
        return new User(name, email, role);
    }
}
