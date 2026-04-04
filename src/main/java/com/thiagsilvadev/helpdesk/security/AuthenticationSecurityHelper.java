package com.thiagsilvadev.helpdesk.security;

import com.thiagsilvadev.helpdesk.entity.Roles;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
class AuthenticationSecurityHelper {

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
}


