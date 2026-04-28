package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketUserInfo;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserTicketControllerTest {

    private TicketCommandService ticketCommandService;
    private TicketQueryService ticketQueryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketCommandService = mock(TicketCommandService.class);
        ticketQueryService = mock(TicketQueryService.class);
        mockMvc = standaloneSetup(new UserTicketController(ticketCommandService, ticketQueryService))
                .setCustomArgumentResolvers(
                        new TestCurrentUserIdArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldCreateTicketForAuthenticatedUser() throws Exception {
        given(ticketCommandService.createByUser(any(CreateUserTicketRequest.class), eq(42L)))
                .willReturn(ticketResponse(100L));

        mockMvc.perform(post("/api/users/tickets")
                        .header("X-Test-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Printer issue",
                                  "description": "Office printer is not working"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/users/tickets/100")))
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void shouldGetAuthenticatedUsersTicket() throws Exception {
        given(ticketQueryService.getOwnTicketById(100L, 42L)).willReturn(ticketResponse(100L));

        mockMvc.perform(get("/api/users/tickets/100").header("X-Test-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.client.id").value(42));
    }

    @Test
    void shouldListAuthenticatedUsersTickets() throws Exception {
        given(ticketQueryService.findTicketsByClientId(42L, PageRequest.of(0, 5)))
                .willReturn(new PageImpl<>(List.of(ticketResponse(100L)), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/users/tickets")
                        .header("X-Test-User-Id", "42")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100));
    }

    @Test
    void shouldUpdateTicket() throws Exception {
        given(ticketCommandService.update(eq(100L), any(UpdateTicketRequest.class)))
                .willReturn(new TicketResponse(
                        100L,
                        "Updated ticket",
                        "Updated description for ticket",
                        TicketStatus.OPEN,
                        TicketPriority.TRIAGE,
                        new TicketUserInfo(42L, "Jane User"),
                        null,
                        Instant.now(),
                        Instant.now(),
                        null
                ));

        mockMvc.perform(put("/api/users/tickets/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated ticket",
                                  "description": "Updated description for ticket"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated ticket"));

        then(ticketCommandService).should().update(eq(100L), any(UpdateTicketRequest.class));
    }

    private TicketResponse ticketResponse(Long id) {
        return new TicketResponse(
                id,
                "Printer issue",
                "Office printer is not working",
                TicketStatus.OPEN,
                TicketPriority.TRIAGE,
                new TicketUserInfo(42L, "Jane User"),
                null,
                Instant.now(),
                Instant.now(),
                null
        );
    }

    private static class TestCurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUserId.class)
                    && Long.class.equals(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return Long.valueOf(webRequest.getHeader("X-Test-User-Id"));
        }
    }
}
