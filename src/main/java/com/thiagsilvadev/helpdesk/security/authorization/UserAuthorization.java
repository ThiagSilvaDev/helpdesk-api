package com.thiagsilvadev.helpdesk.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userAuthorization")
public class UserAuthorization {

    private final AuthorizationSupport authorizationSupport;

    UserAuthorization(AuthorizationSupport authorizationSupport) {
        this.authorizationSupport = authorizationSupport;
    }

    public boolean canCreate(Authentication authentication) {
        return authorizationSupport.isAdmin(authentication);
    }

    public boolean canRead(Authentication authentication) {
        return authorizationSupport.isAdminOrTechnician(authentication);
    }

    public boolean canReadAll(Authentication authentication) {
        return authorizationSupport.isAdmin(authentication);
    }

    public boolean canUpdate(Long id, Authentication authentication) {
        return authorizationSupport.isAdmin(authentication)
                || authorizationSupport.isPrincipalOwner(id, authentication);
    }

    public boolean canChangeRole(Authentication authentication) {
        return authorizationSupport.isAdmin(authentication);
    }

    public boolean canReadUserTickets(Long id, Authentication authentication) {
        if (!authorizationSupport.isAuthenticated(authentication)) {
            return false;
        }

        if (authorizationSupport.isAdminOrTechnician(authentication)) {
            return true;
        }

        return authorizationSupport.isPrincipalOwner(id, authentication);
    }

    public boolean canDeactivate(Authentication authentication) {
        return authorizationSupport.isAdmin(authentication);
    }
}
