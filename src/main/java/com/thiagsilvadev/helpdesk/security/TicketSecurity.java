package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketSecurity")
public class TicketSecurity {

    private final TicketRepository ticketRepository;

    public TicketSecurity(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public boolean canUpdate(Long ticketId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdminOrTechnician = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_TECHNICIAN".equals(authority.getAuthority()));
        if (isAdminOrTechnician) {
            return true;
        }

        boolean isUser = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_USER".equals(authority.getAuthority()));
        if (!isUser) {
            return false;
        }

        return ticketRepository.existsByIdAndClientEmail(ticketId, authentication.getName());
    }
}

