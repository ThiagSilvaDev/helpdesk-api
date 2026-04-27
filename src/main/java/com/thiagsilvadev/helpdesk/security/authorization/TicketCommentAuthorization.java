package com.thiagsilvadev.helpdesk.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketCommentAuthorization")
public class TicketCommentAuthorization {

    private final AuthorizationSupport authorizationSupport;

    TicketCommentAuthorization(AuthorizationSupport authorizationSupport) {
        this.authorizationSupport = authorizationSupport;
    }

    public boolean canModify(Long commentId, Authentication authentication) {
        if (authorizationSupport.isAdmin(authentication)) {
            return true;
        }

        return authorizationSupport.isAuthenticatedCommentAuthor(commentId, authentication);
    }
}
