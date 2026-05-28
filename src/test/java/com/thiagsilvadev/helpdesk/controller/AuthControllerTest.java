package com.thiagsilvadev.helpdesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.thiagsilvadev.helpdesk.dto.auth.AuthLoginRequest;
import com.thiagsilvadev.helpdesk.dto.auth.AuthResponse;
import com.thiagsilvadev.helpdesk.dto.auth.AuthUserResponse;
import com.thiagsilvadev.helpdesk.dto.auth.LogoutRequest;
import com.thiagsilvadev.helpdesk.dto.auth.RefreshTokenRequest;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void shouldAuthenticateAndReturnToken() throws Exception {
        given(authService.authenticate(any(AuthLoginRequest.class)))
                .willReturn(AuthResponse.bearer(
                        "jwt-token",
                        "refresh-token",
                        3600,
                        604800,
                        new AuthUserResponse(1L, "Jane User", Roles.ROLE_USER)));

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {
                                  "email": "jane@helpdesk.local",
                                  "password": "StrongPass@123"
                                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.name").value("Jane User"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));

        then(authService).should().authenticate(any(AuthLoginRequest.class));
    }

    @Test
    void shouldRefreshTokens() throws Exception {
        given(authService.refresh(any(RefreshTokenRequest.class)))
                .willReturn(AuthResponse.refresh("new-jwt-token", "new-refresh-token", 3600, 604800));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.user").doesNotExist());

        then(authService).should().refresh(any(RefreshTokenRequest.class));
    }

    @Test
    void shouldLogoutSession() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"refresh-token\"}"))
                .andExpect(status().isNoContent());

        then(authService).should().logout(any(LogoutRequest.class));
    }
}
