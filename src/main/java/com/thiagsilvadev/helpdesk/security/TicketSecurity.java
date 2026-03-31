package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketSecurity")
public class TicketSecurity {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_TECHNICIAN = "ROLE_TECHNICIAN";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketSecurity(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public boolean canCreate(Long clientId, Authentication authentication) {
        if (isNotAuthenticated(authentication) || !isUser(authentication)) {
            return false;
        }
        return userRepository.existsByIdAndEmail(clientId, authentication.getName());
    }

    public boolean canRead(Long ticketId, Authentication authentication) {
        if (isNotAuthenticated(authentication)) return false;

        if (isAdmin(authentication) || isTechnician(authentication)) return true;

        return isOwner(ticketId, authentication);
    }

    public boolean canUpdate(Long ticketId, Authentication authentication) {
        if (isNotAuthenticated(authentication)) return false;

        if (isAdmin(authentication) || isTechnician(authentication)) return true;

        return isOwner(ticketId, authentication);
    }

    public boolean canCancel(Long ticketId, Authentication authentication) {
        return canUpdate(ticketId, authentication);
    }

    public boolean canClose(Long ticketId, Authentication authentication) {
        if (isNotAuthenticated(authentication)) return false;
        if (isAdmin(authentication)) return true;

        if (isTechnician(authentication)) {
            return ticketRepository.existsByIdAndTechnicianEmail(ticketId, authentication.getName());
        }

        return false;
    }

    public boolean canAssignTechnician(Long technicianId, Authentication authentication) {
        if (isNotAuthenticated(authentication)) return false;
        if (isAdmin(authentication)) return true;

        if (isTechnician(authentication)) {
            return userRepository.existsByIdAndEmail(technicianId, authentication.getName());
        }

        return false;
    }

    private boolean isOwner(Long ticketId, Authentication authentication) {
        if (isNotAuthenticated(authentication)) {
            return false;
        }
        return ticketRepository.existsByIdAndClientEmail(ticketId, authentication.getName());
    }

    private boolean isNotAuthenticated(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private boolean isUser(Authentication authentication) {
        return hasRole(authentication, ROLE_USER);
    }

    private boolean isTechnician(Authentication authentication) {
        return hasRole(authentication, ROLE_TECHNICIAN);
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, ROLE_ADMIN);
    }

}