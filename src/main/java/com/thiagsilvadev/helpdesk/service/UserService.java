package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.mapper.UserRequestMapper;
import com.thiagsilvadev.helpdesk.mapper.UserMapper;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserRequestMapper userRequestMapper;
    private final TicketMapper ticketMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       UserRequestMapper userRequestMapper,
                       TicketMapper ticketMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userRequestMapper = userRequestMapper;
        this.ticketMapper = ticketMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("@userAuthorization.canCreate(authentication)")
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = userRequestMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        return userMapper.toResponse(userRepository.save(user));
    }

    User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @PreAuthorize("@userAuthorization.canRead(authentication)")
    public UserResponse getUserResponseById(Long id) {
        return userMapper.toResponse(getUserById(id));
    }

    @PreAuthorize("@userAuthorization.canReadAll(authentication)")
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @PreAuthorize("@userAuthorization.canUpdate(#id, authentication)")
    public UserResponse update(Long id, UpdateUserRequest request) {
        User existingUser = getUserById(id);

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailAlreadyExistsException(request.email());
        }

        userRequestMapper.applyUpdate(request, existingUser);
        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @PreAuthorize("@userAuthorization.canReadUserTickets(#id, authentication)")
    public List<TicketResponse> getUserTickets(Long id) {
        User user = getUserById(id);
        return user.getTickets().stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @PreAuthorize("@userAuthorization.canDeactivate(authentication)")
    public void deactivate(Long id) {
        User existingUser = getUserById(id);
        existingUser.setActive(false);
        userRepository.save(existingUser);
    }
}
