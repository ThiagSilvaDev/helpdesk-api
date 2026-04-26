package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.UserDTO;
import com.thiagsilvadev.helpdesk.exception.EmailAlreadyExistsException;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.UserMapper;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public UserDTO.Response create(UserDTO.Create.Request request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        com.thiagsilvadev.helpdesk.entity.User user = userMapper.toEntity(request);
        user.changePassword(passwordEncoder.encode(request.password()));

        return userMapper.toResponse(userRepository.save(user));
    }

    public com.thiagsilvadev.helpdesk.entity.User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @PreAuthorize("@userAuthorization.canRead(authentication)")
    public UserDTO.Response getUserResponseById(Long id) {
        return userMapper.toResponse(getUserById(id));
    }

    @PreAuthorize("@userAuthorization.canReadAll(authentication)")
    public Page<UserDTO.Response> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    @PreAuthorize("@userAuthorization.canUpdate(#id, authentication)")
    @Transactional
    public UserDTO.Response update(Long id, UserDTO.Update.Request request) {
        com.thiagsilvadev.helpdesk.entity.User existingUser = getUserById(id);
        userMapper.applyUpdate(request, existingUser);
        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @PreAuthorize("@userAuthorization.canChangeRole(authentication)")
    @Transactional
    public UserDTO.Response changeRole(Long id, UserDTO.ChangeRole.Request request) {
        com.thiagsilvadev.helpdesk.entity.User existingUser = getUserById(id);
        existingUser.changeRole(request.role());
        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @PreAuthorize("@userAuthorization.canDeactivate(authentication)")
    @Transactional
    public void deactivate(Long id) {
        com.thiagsilvadev.helpdesk.entity.User existingUser = getUserById(id);
        existingUser.deactivate();
        userRepository.save(existingUser);
    }
}
