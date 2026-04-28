package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.user.ChangeUserRoleRequest;
import com.thiagsilvadev.helpdesk.dto.user.CreateUserRequest;
import com.thiagsilvadev.helpdesk.dto.user.UpdateUserNameRequest;
import com.thiagsilvadev.helpdesk.dto.user.UserResponse;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.UserMapper;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("@userAuthorization.canCreate(authentication)")
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = userMapper.toEntity(request, encodedPassword);

        return userMapper.toResponse(userRepository.save(user));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @PreAuthorize("@userAuthorization.canRead(authentication)")
    public UserResponse getUserResponseById(Long id) {
        return userMapper.toResponse(getUserById(id));
    }

    @PreAuthorize("@userAuthorization.canReadAll(authentication)")
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @PreAuthorize("@userAuthorization.canUpdate(#id, authentication)")
    @Transactional
    public UserResponse update(Long id, UpdateUserNameRequest request) {
        User existingUser = getUserById(id);
        userMapper.applyUpdate(request, existingUser);
        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @PreAuthorize("@userAuthorization.canChangeRole(authentication)")
    @Transactional
    public UserResponse changeRole(Long id, ChangeUserRoleRequest request) {
        User existingUser = getUserById(id);
        existingUser.changeRole(request.role());
        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @PreAuthorize("@userAuthorization.canDeactivate(authentication)")
    @Transactional
    public void deactivate(Long id) {
        User existingUser = getUserById(id);
        existingUser.deactivate();
        userRepository.save(existingUser);
    }
}
