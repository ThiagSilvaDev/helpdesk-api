package com.thiagsilvadev.helpdesk.security.authorization;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
class AuthorizationSupport {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    AuthorizationSupport(TicketRepository ticketRepository,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    boolean hasRole(Authentication authentication, Roles role) {
        return isAuthenticated(authentication)
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.name().equals(authority.getAuthority()));
    }

    boolean isUser(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_USER);
    }

    boolean isTechnician(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_TECHNICIAN);
    }

    boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_ADMIN);
    }

    boolean isAdminOrTechnician(Authentication authentication) {
        return isAdmin(authentication) || isTechnician(authentication);
    }

    boolean isPrincipalOwner(Long id, Authentication authentication) {
        if (!isAuthenticated(authentication) || id == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        return id.equals(userPrincipal.getId());
    }

    boolean isAuthenticatedUserByIdAndEmail(Long id, Authentication authentication) {
        String email = getAuthenticatedEmail(authentication);
        if (id == null || email == null) {
            return false;
        }

        return userRepository.existsByIdAndEmail(id, email);
    }

    boolean isAuthenticatedTicketOwner(Long ticketId, Authentication authentication) {
        if (ticketId == null || !isAuthenticated(authentication)) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        return ticketRepository.existsByIdAndClientId(ticketId, userPrincipal.getId());
    }

    boolean isAuthenticatedTechnicianAssignedToTicket(Long ticketId, Authentication authentication) {
        String email = getAuthenticatedEmail(authentication);
        if (ticketId == null || email == null) {
            return false;
        }

        return ticketRepository.existsByIdAndTechnicianEmail(ticketId, email);
    }

    private String getAuthenticatedEmail(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        String email = authentication.getName();
        if (email == null || email.isBlank()) {
            return null;
        }

        return email;
    }
}


