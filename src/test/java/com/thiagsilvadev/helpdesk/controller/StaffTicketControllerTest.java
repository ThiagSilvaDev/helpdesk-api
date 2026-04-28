package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
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

class StaffTicketControllerTest {

    private TicketCommandService ticketCommandService;
    private TicketQueryService ticketQueryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketCommandService = mock(TicketCommandService.class);
        ticketQueryService = mock(TicketQueryService.class);
        mockMvc = standaloneSetup(new StaffTicketController(ticketCommandService, ticketQueryService))
                .setCustomArgumentResolvers(
                        new TestCurrentUserIdArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldCreateTicketAsStaff() throws Exception {
        given(ticketCommandService.createByStaff(any(TicketDTO.Create.StaffRequest.class)))
                .willReturn(ticketResponse(100L, TicketPriority.HIGH, null));

        mockMvc.perform(post("/api/staff/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "VPN issue",
                                  "description": "Cannot connect to corporate VPN",
                                  "requesterId": 42,
                                  "priority": "HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/staff/tickets/100")))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void shouldListTicketsForStaffWithCriteria() throws Exception {
        given(ticketQueryService.findAll(
                new TicketDTO.Search.Criteria(TicketStatus.OPEN, TicketPriority.TRIAGE),
                PageRequest.of(0, 10)
        )).willReturn(new PageImpl<>(List.of(ticketResponse(100L, TicketPriority.TRIAGE, null)), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/staff/tickets")
                        .param("status", "OPEN")
                        .param("priority", "TRIAGE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void shouldUpdateTicketPriority() throws Exception {
        given(ticketCommandService.updatePriority(eq(100L), any(TicketDTO.UpdatePriority.Request.class)))
                .willReturn(ticketResponse(100L, TicketPriority.URGENT, null));

        mockMvc.perform(patch("/api/staff/tickets/100/priority")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"URGENT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("URGENT"));
    }

    @Test
    void shouldAssignAuthenticatedTechnicianWhenRequestTechnicianIsNull() throws Exception {
        TicketDTO.Response.UserInfo technician = new TicketDTO.Response.UserInfo(77L, "Tech User");
        given(ticketCommandService.assignTechnician(100L, null, 77L))
                .willReturn(ticketResponse(100L, TicketPriority.TRIAGE, technician));

        mockMvc.perform(patch("/api/staff/tickets/100/technician")
                        .header("X-Test-User-Id", "77")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technician.id").value(77));
    }

    @Test
    void shouldCloseTicket() throws Exception {
        mockMvc.perform(patch("/api/staff/tickets/100/close"))
                .andExpect(status().isNoContent());

        then(ticketCommandService).should().close(100L);
    }

    @Test
    void shouldCancelTicket() throws Exception {
        mockMvc.perform(patch("/api/staff/tickets/100/cancel"))
                .andExpect(status().isNoContent());

        then(ticketCommandService).should().cancel(100L);
    }

    private TicketDTO.Response ticketResponse(Long id, TicketPriority priority, TicketDTO.Response.UserInfo technician) {
        return new TicketDTO.Response(
                id,
                "VPN issue",
                "Cannot connect to corporate VPN",
                technician == null ? TicketStatus.OPEN : TicketStatus.IN_PROGRESS,
                priority,
                new TicketDTO.Response.UserInfo(42L, "Jane User"),
                technician,
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
