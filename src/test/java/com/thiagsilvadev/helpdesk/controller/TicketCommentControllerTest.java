package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketCommentDTO;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommentService;
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

class TicketCommentControllerTest {

    private TicketCommentService ticketCommentService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ticketCommentService = mock(TicketCommentService.class);
        mockMvc = standaloneSetup(new TicketCommentController(ticketCommentService))
                .setCustomArgumentResolvers(
                        new TestCurrentUserIdArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldListTicketComments() throws Exception {
        given(ticketCommentService.findByTicketId(100L, PageRequest.of(0, 5)))
                .willReturn(new PageImpl<>(List.of(commentResponse()), PageRequest.of(0, 5), 1));

        mockMvc.perform(get("/api/tickets/100/comments")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(200));
    }

    @Test
    void shouldCreateTicketComment() throws Exception {
        given(ticketCommentService.create(eq(100L), any(TicketCommentDTO.Create.Request.class), eq(42L)))
                .willReturn(commentResponse());

        mockMvc.perform(post("/api/tickets/100/comments")
                        .header("X-Test-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Need help with this ticket\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/tickets/100/comments/200")))
                .andExpect(jsonPath("$.content").value("Need help with this ticket"));
    }

    @Test
    void shouldUpdateTicketComment() throws Exception {
        given(ticketCommentService.update(eq(100L), eq(200L), any(TicketCommentDTO.Update.Request.class)))
                .willReturn(new TicketCommentDTO.Response(
                        200L,
                        100L,
                        new TicketCommentDTO.Response.AuthorInfo(42L, "Jane User"),
                        "Updated comment",
                        Instant.now(),
                        Instant.now()
                ));

        mockMvc.perform(put("/api/tickets/100/comments/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));
    }

    @Test
    void shouldDeleteTicketComment() throws Exception {
        mockMvc.perform(delete("/api/tickets/100/comments/200"))
                .andExpect(status().isNoContent());

        then(ticketCommentService).should().delete(100L, 200L);
    }

    private TicketCommentDTO.Response commentResponse() {
        return new TicketCommentDTO.Response(
                200L,
                100L,
                new TicketCommentDTO.Response.AuthorInfo(42L, "Jane User"),
                "Need help with this ticket",
                Instant.now(),
                Instant.now()
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
