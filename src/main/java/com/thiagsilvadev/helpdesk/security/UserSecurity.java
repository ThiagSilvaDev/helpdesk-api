package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean canReadUserTickets(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdminOrTechnician = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TECHNICIAN".equals(authority.getAuthority()));
        if (isAdminOrTechnician) {
            return true;
        }

        return userRepository.existsByIdAndEmail(userId, authentication.getName());
    }
}

