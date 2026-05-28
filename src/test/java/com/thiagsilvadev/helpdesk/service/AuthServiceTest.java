package com.thiagsilvadev.helpdesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthenticatedUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LogoutRequest;
import com.thiagsilvadev.helpdesk.dto.auth.RefreshTokenRequest;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.security.authentication.JwtService;
import com.thiagsilvadev.helpdesk.security.authentication.RefreshTokenService;
import com.thiagsilvadev.helpdesk.security.authentication.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@helpdesk.local";
    private static final String PASSWORD = "SecurePassword@123";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Authenticate {

        private AuthLoginRequest request;

        @BeforeEach
        void setUp() {
            request = new AuthLoginRequest(EMAIL, PASSWORD);
        }

        @Test
        void shouldAuthenticateUserAndReturnToken() {
            User user = persistedUser("John User", EMAIL, Roles.ROLE_USER);
            UserPrincipal principal = new UserPrincipal(user);

            Authentication authentication = createAuthenticationMock(principal);
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtService.generateToken(principal)).willReturn(JWT_TOKEN);
            given(jwtService.getExpirationSeconds()).willReturn(3600L);
            given(refreshTokenService.issue(USER_ID))
                    .willReturn(new RefreshTokenService.RefreshTokenIssue(REFRESH_TOKEN, 604800L));

            AuthResponse response = authService.authenticate(request);

            assertThat(response.accessToken()).isEqualTo(JWT_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(response.expiresIn()).isEqualTo(3600L);
            assertThat(response.refreshExpiresIn()).isEqualTo(604800L);
            assertThat(response.user())
                    .returns(USER_ID, AuthUserResponse::id)
                    .returns("John User", AuthUserResponse::name)
                    .returns(Roles.ROLE_USER, AuthUserResponse::role);
            then(authenticationManager).should().authenticate(any());
            then(jwtService).should().generateToken(principal);
            then(refreshTokenService).should().issue(USER_ID);
        }

        @Test
        void shouldGenerateTokenWithUserRole() {
            User user = persistedUser("Tech User", EMAIL, Roles.ROLE_TECHNICIAN);
            UserPrincipal principal = new UserPrincipal(user);

            Authentication authentication = createAuthenticationMock(principal);
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtService.generateToken(principal)).willReturn(JWT_TOKEN);
            given(jwtService.getExpirationSeconds()).willReturn(3600L);
            given(refreshTokenService.issue(USER_ID))
                    .willReturn(new RefreshTokenService.RefreshTokenIssue(REFRESH_TOKEN, 604800L));

            AuthResponse response = authService.authenticate(request);

            assertThat(response.accessToken()).isEqualTo(JWT_TOKEN);
            assertThat(response.user().role()).isEqualTo(Roles.ROLE_TECHNICIAN);
            then(jwtService).should().generateToken(principal);
        }
    }

    @Test
    void shouldRefreshTokens() {
        User user = persistedUser("John User", EMAIL, Roles.ROLE_USER);
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        given(refreshTokenService.rotate(REFRESH_TOKEN))
                .willReturn(new RefreshTokenService.RefreshTokenRotation(USER_ID, "new-refresh-token", 604800L));
        given(userService.getUserById(USER_ID)).willReturn(user);
        given(jwtService.generateToken(any(UserPrincipal.class))).willReturn("new-jwt-token");
        given(jwtService.getExpirationSeconds()).willReturn(3600L);

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-jwt-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.user()).isNull();
        then(refreshTokenService).should().rotate(REFRESH_TOKEN);
    }

    @Test
    void shouldLogoutByRevokingRefreshToken() {
        authService.logout(new LogoutRequest(REFRESH_TOKEN));

        then(refreshTokenService).should().revoke(REFRESH_TOKEN);
    }

    @Test
    void shouldReturnAuthenticatedUserProfile() {
        User user = persistedUser("John User", EMAIL, Roles.ROLE_USER);
        given(userService.getUserById(USER_ID)).willReturn(user);

        AuthenticatedUserResponse response = authService.getAuthenticatedUser(USER_ID);

        assertThat(response)
                .returns(USER_ID, AuthenticatedUserResponse::id)
                .returns("John User", AuthenticatedUserResponse::name)
                .returns(EMAIL, AuthenticatedUserResponse::email)
                .returns(Roles.ROLE_USER, AuthenticatedUserResponse::role)
                .returns(true, AuthenticatedUserResponse::active);
    }

    private Authentication createAuthenticationMock(UserPrincipal principal) {
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);
        return authentication;
    }

    private User persistedUser(String name, String email, Roles role) {
        User user = new User(name, email, "encoded-password", role);
        ReflectionTestUtils.setField(user, "id", AuthServiceTest.USER_ID);
        return user;
    }
}
