package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.notification.NotificationResponse;
import com.thiagsilvadev.helpdesk.dto.notification.UnreadNotificationCountResponse;
import com.thiagsilvadev.helpdesk.entity.notification.NotificationType;
import com.thiagsilvadev.helpdesk.security.web.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.mock;

class NotificationControllerTest {

    private NotificationService notificationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        mockMvc = standaloneSetup(new NotificationController(notificationService))
                .setCustomArgumentResolvers(
                        new TestCurrentUserIdArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldListCurrentUsersNotifications() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        given(notificationService.findForUser(42L, true, pageable))
                .willReturn(new PageImpl<>(List.of(notification()), pageable, 1));

        mockMvc.perform(get("/api/notifications")
                        .header("X-Test-User-Id", "42")
                        .param("unreadOnly", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].type").value("TICKET_ASSIGNED"))
                .andExpect(jsonPath("$.content[0].read").value(false));
    }

    @Test
    void shouldReturnUnreadCount() throws Exception {
        given(notificationService.countUnread(42L)).willReturn(new UnreadNotificationCountResponse(3));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("X-Test-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(3));
    }

    @Test
    void shouldMarkNotificationRead() throws Exception {
        NotificationResponse response = new NotificationResponse(
                1L,
                NotificationType.TICKET_ASSIGNED,
                "Ticket assigned",
                "Ticket #100 was assigned",
                100L,
                null,
                77L,
                true,
                Instant.now(),
                Instant.now()
        );
        given(notificationService.markAsRead(1L, 42L)).willReturn(response);

        mockMvc.perform(patch("/api/notifications/1/read")
                        .header("X-Test-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void shouldMarkAllNotificationsRead() throws Exception {
        mockMvc.perform(patch("/api/notifications/read-all")
                        .header("X-Test-User-Id", "42"))
                .andExpect(status().isNoContent());

        then(notificationService).should().markAllAsRead(42L);
    }

    private NotificationResponse notification() {
        return new NotificationResponse(
                1L,
                NotificationType.TICKET_ASSIGNED,
                "Ticket assigned",
                "Ticket #100 was assigned",
                100L,
                null,
                77L,
                false,
                null,
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
