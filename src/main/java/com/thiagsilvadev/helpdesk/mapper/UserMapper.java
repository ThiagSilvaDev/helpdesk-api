package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.UserDTO;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO.Response toResponse(com.thiagsilvadev.helpdesk.entity.User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO.Response(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        );
    }

    public User toEntity(UserDTO.Create.Request request) {
        if (request == null) {
            return null;
        }

        return new com.thiagsilvadev.helpdesk.entity.User(request.name(), request.email(), request.password(), request.role());
    }

    public void applyUpdate(UserDTO.Update.Request request, com.thiagsilvadev.helpdesk.entity.User user) {
        if (request == null || user == null) {
            return;
        }

        user.rename(request.name());
    }
}
