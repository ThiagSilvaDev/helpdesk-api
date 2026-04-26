package com.thiagsilvadev.helpdesk.integration;

import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.UserRepository;
import com.thiagsilvadev.helpdesk.security.JwtService;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserManagementIT extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User admin;
    private User user;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        admin = saveUser("Admin User", "admin@helpdesk.local", Roles.ROLE_ADMIN);
        user = saveUser("Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
    }

    @Test
    void adminShouldCreateAndListUsers() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Tech User",
                                  "email": "tech@helpdesk.local",
                                  "password": "StrongPass@123",
                                  "role": "ROLE_TECHNICIAN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("/api/users/")))
                .andExpect(jsonPath("$.email").value("tech@helpdesk.local"))
                .andExpect(jsonPath("$.role").value("ROLE_TECHNICIAN"));

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3));
    }

    @Test
    void regularUserShouldUpdateSelfButNotListUsers() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/name", user.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jane Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Updated"));

        assertThat(userRepository.findById(user.getId()).orElseThrow().getName()).isEqualTo("Jane Updated");

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotAccessUserManagement() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Authentication is required to access this resource."));
    }

    private User saveUser(String name, String email, Roles role) {
        return userRepository.save(new User(name, email, passwordEncoder.encode("StrongPass@123"), role));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(new UserPrincipal(user));
    }
}
