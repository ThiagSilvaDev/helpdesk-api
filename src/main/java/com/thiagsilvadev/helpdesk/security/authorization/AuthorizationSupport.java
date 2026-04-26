package com.thiagsilvadev.helpdesk.security.authorization;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
class AuthorizationSupport {

    private final TicketRepository ticketRepository;

    AuthorizationSupport(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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
        Long userId = getAuthenticatedUserId(authentication);
        return id != null && id.equals(userId);
    }

    boolean isAuthenticatedTicketOwner(Long ticketId, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        if (ticketId == null || userId == null) {
            return false;
        }

        return ticketRepository.existsByIdAndClientId(ticketId, userId);
    }

    boolean isAuthenticatedTechnicianAssignedToTicket(Long ticketId, Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        if (ticketId == null || userId == null) {
            return false;
        }

        return ticketRepository.existsByIdAndTechnicianId(ticketId, userId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            return null;
        }

        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

