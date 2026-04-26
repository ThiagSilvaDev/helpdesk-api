package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.AuthDTO;
import com.thiagsilvadev.helpdesk.security.JwtService;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "user@helpdesk.local";
    private static final String PASSWORD = "SecurePassword@123";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Authenticate {

        @Test
        void shouldAuthenticateUserAndReturnToken() {
            AuthDTO.Login.Request request = new AuthDTO.Login.Request(EMAIL, PASSWORD);
            User user = persistedUser(USER_ID, "John User", EMAIL, Roles.ROLE_USER);
            UserPrincipal principal = new UserPrincipal(user);

            Authentication authentication = createAuthenticationMock(principal);
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtService.generateToken(principal)).willReturn(JWT_TOKEN);

            AuthDTO.Response response = authService.authenticate(request);

            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            then(authenticationManager).should().authenticate(any());
            then(jwtService).should().generateToken(principal);
        }

        @Test
        void shouldGenerateTokenWithUserRole() {
            AuthDTO.Login.Request request = new AuthDTO.Login.Request(EMAIL, PASSWORD);
            User user = persistedUser(USER_ID, "Tech User", EMAIL, Roles.ROLE_TECHNICIAN);
            UserPrincipal principal = new UserPrincipal(user);

            Authentication authentication = createAuthenticationMock(principal);
            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(jwtService.generateToken(principal)).willReturn(JWT_TOKEN);

            AuthDTO.Response response = authService.authenticate(request);

            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            then(jwtService).should().generateToken(principal);
        }
    }

    private Authentication createAuthenticationMock(UserPrincipal principal) {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(principal);
        return authentication;
    }

    private User persistedUser(Long id, String name, String email, Roles role) {
        User user = new User(name, email, "encoded-password", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
