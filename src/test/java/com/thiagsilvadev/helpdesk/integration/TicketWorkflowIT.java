package com.thiagsilvadev.helpdesk.integration;

import com.jayway.jsonpath.JsonPath;
import com.thiagsilvadev.helpdesk.entity.Roles;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
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

class TicketWorkflowIT extends PostgresIntegrationTest {

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

    private User client;
    private User technician;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
        userRepository.deleteAll();
        client = saveUser("Jane User", "jane@helpdesk.local", Roles.ROLE_USER);
        technician = saveUser("Tech User", "tech@helpdesk.local", Roles.ROLE_TECHNICIAN);
    }

    @Test
    void userCanOpenTicketAndTechnicianCanAssignAndCloseIt() throws Exception {
        String createResponse = mockMvc.perform(post("/api/users/tickets")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Printer issue",
                                  "description": "Office printer is not working"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.priority").value("TRIAGE"))
                .andExpect(jsonPath("$.client.id").value(client.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number ticketId = JsonPath.read(createResponse, "$.id");

        mockMvc.perform(get("/api/users/tickets/{ticketId}", ticketId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, bearer(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.intValue()));

        mockMvc.perform(patch("/api/staff/tickets/{ticketId}/technician", ticketId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, bearer(technician))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.technician.id").value(technician.getId()));

        mockMvc.perform(patch("/api/staff/tickets/{ticketId}/close", ticketId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, bearer(technician)))
                .andExpect(status().isNoContent());

        assertThat(ticketRepository.findById(ticketId.longValue()).orElseThrow().getStatus()).isEqualTo(TicketStatus.CLOSED);
    }

    @Test
    void userShouldNotReadAnotherUsersTicket() throws Exception {
        User otherClient = saveUser("Other User", "other@helpdesk.local", Roles.ROLE_USER);

        String createResponse = mockMvc.perform(post("/api/users/tickets")
                        .header(HttpHeaders.AUTHORIZATION, bearer(client))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Laptop issue",
                                  "description": "Laptop does not turn on"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number ticketId = JsonPath.read(createResponse, "$.id");

        mockMvc.perform(get("/api/users/tickets/{ticketId}", ticketId.longValue())
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherClient)))
                .andExpect(status().isForbidden());
    }

    private User saveUser(String name, String email, Roles role) {
        return userRepository.save(new User(name, email, passwordEncoder.encode("StrongPass@123"), role));
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(new UserPrincipal(user));
    }
}
