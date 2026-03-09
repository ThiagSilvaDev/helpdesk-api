package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = request.toEntity();
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse update(Long id, UpdateUserRequest request) {
        User existingUser = getUserById(id);

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailAlreadyExistsException(request.email());
        }

        existingUser.setName(request.name());
        existingUser.setEmail(request.email());
        existingUser.setRole(request.role());

        return UserResponse.fromEntity(userRepository.save(existingUser));
    }

    public List<TicketResponse> getUserTickets(Long userId) {
        User user = getUserById(userId);
        return user.getTickets().stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public void delete(Long id) {
        User existingUser = getUserById(id);

        userRepository.delete(existingUser);
    }
}
