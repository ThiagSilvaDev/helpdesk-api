package com.thiagsilvadev.helpdesk.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final AuthenticationSecurityHelper authenticationSecurityHelper;

    UserSecurity(AuthenticationSecurityHelper authenticationSecurityHelper) {
        this.authenticationSecurityHelper = authenticationSecurityHelper;
    }

    public boolean canCreate(Authentication authentication) {
        return authenticationSecurityHelper.isAdmin(authentication);
    }

    public boolean canReadAll(Authentication authentication) {
        return authenticationSecurityHelper.isAdmin(authentication);
    }

    public boolean canRead(Authentication authentication) {
        return authenticationSecurityHelper.isAdminOrTechnician(authentication);
    }

    public boolean canUpdate(Long id, Authentication authentication) {
        return authenticationSecurityHelper.isAdmin(authentication)
                || authenticationSecurityHelper.isPrincipalOwner(id, authentication);
    }

    public boolean canReadUserTickets(Long id, Authentication authentication) {
        if (!authenticationSecurityHelper.isAuthenticated(authentication)) {
            return false;
        }

        if (authenticationSecurityHelper.isAdminOrTechnician(authentication)) {
            return true;
        }

        return authenticationSecurityHelper.isPrincipalOwner(id, authentication);
    }

    public boolean canDeactivate(Authentication authentication) {
        return authenticationSecurityHelper.isAdmin(authentication);
    }
}

