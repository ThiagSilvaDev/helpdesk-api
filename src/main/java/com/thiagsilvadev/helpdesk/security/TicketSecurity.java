package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketSecurity")
public class TicketSecurity {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuthenticationSecurityHelper authenticationSecurityHelper;

    TicketSecurity(TicketRepository ticketRepository,
                   UserRepository userRepository,
                   AuthenticationSecurityHelper authenticationSecurityHelper) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.authenticationSecurityHelper = authenticationSecurityHelper;
    }

    public boolean canCreate(Authentication authentication) {
        return authenticationSecurityHelper.isUser(authentication);
    }

    public boolean canRead(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    public boolean canUpdate(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    public boolean canCancel(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    private boolean canReadUpdateOrCancel(Long ticketId, Authentication authentication) {
        if (authenticationSecurityHelper.isAdminOrTechnician(authentication)) return true;

        return isOwner(ticketId, authentication);
    }

    public boolean canClose(Long ticketId, Authentication authentication) {
        if (authenticationSecurityHelper.isAdmin(authentication)) return true;

        if (authenticationSecurityHelper.isTechnician(authentication)) {
            return ticketRepository.existsByIdAndTechnicianEmail(ticketId, authentication.getName());
        }

        return false;
    }

    public boolean canAssignTechnician(Long technicianId, Authentication authentication) {
        if (authenticationSecurityHelper.isAdmin(authentication)) return true;

        if (authenticationSecurityHelper.isTechnician(authentication)) {
            return userRepository.existsByIdAndEmail(technicianId, authentication.getName());
        }

        return false;
    }

    private boolean isOwner(Long ticketId, Authentication authentication) {
        if (!authenticationSecurityHelper.isAuthenticated(authentication)) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return false;
        }

        return ticketRepository.existsByIdAndClientId(ticketId, userPrincipal.getId());
    }
}