package com.thiagsilvadev.helpdesk.api;

import com.thiagsilvadev.helpdesk.dto.notification.NotificationResponse;
import com.thiagsilvadev.helpdesk.dto.notification.UnreadNotificationCountResponse;
import com.thiagsilvadev.helpdesk.security.web.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Authenticated user's in-app notifications")
@SecurityRequirement(name = "bearerAuth")
@ApiSecurityResponseErrors
public interface NotificationApi {

    @GetMapping
    @Operation(operationId = "listNotifications")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications retrieved")
    })
    ResponseEntity<Page<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @CurrentUserId Long userId,
            @ParameterObject Pageable pageable
    );

    @GetMapping("/unread-count")
    @Operation(operationId = "countUnreadNotifications")
    ResponseEntity<UnreadNotificationCountResponse> countUnreadNotifications(@CurrentUserId Long userId);

    @PatchMapping("/{id}/read")
    @Operation(operationId = "markNotificationRead")
    @ApiByIdErrors
    ResponseEntity<NotificationResponse> markNotificationRead(
            @PathVariable Long id,
            @CurrentUserId Long userId
    );

    @PatchMapping("/read-all")
    @Operation(operationId = "markAllNotificationsRead")
    ResponseEntity<Void> markAllNotificationsRead(@CurrentUserId Long userId);
}
