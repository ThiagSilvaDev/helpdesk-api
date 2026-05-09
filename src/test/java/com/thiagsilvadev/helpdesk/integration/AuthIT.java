package com.thiagsilvadev.helpdesk.integration;

import com.jayway.jsonpath.JsonPath;
import com.thiagsilvadev.helpdesk.entity.user.Roles;
import com.thiagsilvadev.helpdesk.entity.user.User;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIT extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldAuthenticateUserAndIssueUsableJwt() throws Exception {
        userRepository.save(new User(
                "Jane User",
                "jane@helpdesk.local",
                passwordEncoder.encode("StrongPass@123"),
                Roles.ROLE_USER
        ));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@helpdesk.local",
                                  "password": "StrongPass@123"
                                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Jane User"))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = JsonPath.read(response, "$.accessToken");
        assertThat(jwtDecoder.decode(token).getClaimAsStringList("roles")).containsExactly(Roles.ROLE_USER.name());
    }

    @Test
    void shouldRejectBadCredentials() throws Exception {
        userRepository.save(new User(
                "Jane User",
                "jane@helpdesk.local",
                passwordEncoder.encode("StrongPass@123"),
                Roles.ROLE_USER
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@helpdesk.local",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }
}
