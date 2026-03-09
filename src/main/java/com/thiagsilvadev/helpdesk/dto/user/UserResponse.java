package com.thiagsilvadev.helpdesk.dto.user;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Roles role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}

