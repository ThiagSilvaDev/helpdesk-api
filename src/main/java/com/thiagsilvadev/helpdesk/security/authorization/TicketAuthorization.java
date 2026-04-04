package com.thiagsilvadev.helpdesk.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketAuthorization")
public class TicketAuthorization {

    private final AuthorizationSupport authorizationSupport;

    TicketAuthorization(AuthorizationSupport authorizationSupport) {
        this.authorizationSupport = authorizationSupport;
    }

    public boolean canCreate(Authentication authentication) {
        return authorizationSupport.isUser(authentication);
    }

    public boolean canRead(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    public boolean canUpdate(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    public boolean canUpdatePriority(Authentication authentication) {
        return authorizationSupport.isAdminOrTechnician(authentication);
    }

    public boolean canCancel(Long ticketId, Authentication authentication) {
        return canReadUpdateOrCancel(ticketId, authentication);
    }

    private boolean canReadUpdateOrCancel(Long ticketId, Authentication authentication) {
        if (authorizationSupport.isAdminOrTechnician(authentication)) return true;

        return authorizationSupport.isAuthenticatedTicketOwner(ticketId, authentication);
    }

    public boolean canClose(Long ticketId, Authentication authentication) {
        if (authorizationSupport.isAdmin(authentication)) return true;

        if (authorizationSupport.isTechnician(authentication)) {
            return authorizationSupport.isAuthenticatedTechnicianAssignedToTicket(ticketId, authentication);
        }

        return false;
    }

    public boolean canAssignTechnician(Long technicianId, Authentication authentication) {
        if (authorizationSupport.isAdmin(authentication)) return true;

        if (authorizationSupport.isTechnician(authentication)) {
            return authorizationSupport.isAuthenticatedUserByIdAndEmail(technicianId, authentication);
        }

        return false;
    }
}