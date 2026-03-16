package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserRequest;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserRequestMapper {

    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        return new User(request.name(), request.email(), request.role());
    }

    public void applyUpdate(UpdateUserRequest request, User user) {
        if (request == null || user == null) {
            return;
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(request.role());
    }
}

