package com.thiagsilvadev.helpdesk.security.authorization;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.repository.TicketCommentRepository;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthorizationTest {

    private TicketRepository ticketRepository;
    private TicketCommentRepository ticketCommentRepository;
    private UserAuthorization userAuthorization;
    private TicketAuthorization ticketAuthorization;
    private TicketCommentAuthorization ticketCommentAuthorization;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        ticketCommentRepository = mock(TicketCommentRepository.class);
        AuthorizationSupport support = new AuthorizationSupport(ticketRepository, ticketCommentRepository);
        userAuthorization = new UserAuthorization(support);
        ticketAuthorization = new TicketAuthorization(support);
        ticketCommentAuthorization = new TicketCommentAuthorization(support);
    }

    @Test
    void shouldAllowOnlyAdminsToCreateUsersAndReadAllUsers() {
        Authentication admin = authentication("1", Roles.ROLE_ADMIN);
        Authentication technician = authentication("2", Roles.ROLE_TECHNICIAN);

        assertThat(userAuthorization.canCreate(admin)).isTrue();
        assertThat(userAuthorization.canReadAll(admin)).isTrue();
        assertThat(userAuthorization.canCreate(technician)).isFalse();
        assertThat(userAuthorization.canReadAll(technician)).isFalse();
    }

    @Test
    void shouldAllowAdminOrSelfToUpdateUser() {
        assertThat(userAuthorization.canUpdate(99L, authentication("1", Roles.ROLE_ADMIN))).isTrue();
        assertThat(userAuthorization.canUpdate(42L, authentication("42", Roles.ROLE_USER))).isTrue();
        assertThat(userAuthorization.canUpdate(99L, authentication("42", Roles.ROLE_USER))).isFalse();
    }

    @Test
    void shouldAllowTicketOwnerOrStaffToReadUpdateOrCancelTicket() {
        given(ticketRepository.existsByIdAndClientId(100L, 42L)).willReturn(true);

        assertThat(ticketAuthorization.canRead(100L, authentication("42", Roles.ROLE_USER))).isTrue();
        assertThat(ticketAuthorization.canUpdate(100L, authentication("7", Roles.ROLE_TECHNICIAN))).isTrue();
        assertThat(ticketAuthorization.canCancel(100L, authentication("1", Roles.ROLE_ADMIN))).isTrue();
        assertThat(ticketAuthorization.canRead(100L, authentication("99", Roles.ROLE_USER))).isFalse();
    }

    @Test
    void shouldAllowAssignedTechnicianOrAdminToCloseTicket() {
        given(ticketRepository.existsByIdAndTechnicianId(100L, 7L)).willReturn(true);

        assertThat(ticketAuthorization.canClose(100L, authentication("1", Roles.ROLE_ADMIN))).isTrue();
        assertThat(ticketAuthorization.canClose(100L, authentication("7", Roles.ROLE_TECHNICIAN))).isTrue();
        assertThat(ticketAuthorization.canClose(100L, authentication("8", Roles.ROLE_TECHNICIAN))).isFalse();
        assertThat(ticketAuthorization.canClose(100L, authentication("42", Roles.ROLE_USER))).isFalse();
    }

    @Test
    void shouldDenyOwnershipChecksWhenPrincipalSubjectIsInvalid() {
        assertThat(userAuthorization.canUpdate(42L, authentication("not-a-number", Roles.ROLE_USER))).isFalse();
        assertThat(ticketAuthorization.canRead(100L, authentication("not-a-number", Roles.ROLE_USER))).isFalse();
    }

    @Test
    void shouldAllowAdminOrCommentAuthorToModifyComment() {
        given(ticketCommentRepository.existsByIdAndAuthorId(50L, 42L)).willReturn(true);

        assertThat(ticketCommentAuthorization.canModify(50L, authentication("1", Roles.ROLE_ADMIN))).isTrue();
        assertThat(ticketCommentAuthorization.canModify(50L, authentication("42", Roles.ROLE_USER))).isTrue();
        assertThat(ticketCommentAuthorization.canModify(50L, authentication("99", Roles.ROLE_USER))).isFalse();
    }

    private Authentication authentication(String subject, Roles role) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return new TestingAuthenticationToken(
                jwt,
                null,
                List.of(new SimpleGrantedAuthority(role.name()))
        );
    }
}
