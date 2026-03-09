package com.thiagsilvadev.helpdesk.dto.ticket;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.User;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        UserInfo client,
        UserInfo technician,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {
    public record UserInfo(Long id, String name) {
        public static UserInfo fromEntity(User user) {
            if (user == null) return null;
            return new UserInfo(user.getId(), user.getName());
        }
    }

    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                UserInfo.fromEntity(ticket.getClient()),
                UserInfo.fromEntity(ticket.getTechnician()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }
}

