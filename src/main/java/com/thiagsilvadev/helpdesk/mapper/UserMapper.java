package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.UserDto;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto.UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return new UserDto.UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }

    public User toEntity(UserDto.CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return new User(request.name(), request.email(), request.password(), request.role());
    }

    public void applyUpdate(UserDto.UpdateUserRequest request, User user) {
        if (request == null || user == null) {
            return;
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
    }
}
