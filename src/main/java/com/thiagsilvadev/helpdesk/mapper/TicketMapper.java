package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketUserInfo;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(com.thiagsilvadev.helpdesk.entity.Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                toUserInfo(ticket.getClient()),
                toUserInfo(ticket.getTechnician()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }

    public com.thiagsilvadev.helpdesk.entity.Ticket toEntityFromUserRequest(CreateUserTicketRequest userRequest, User client) {
        if (userRequest == null || client == null) {
            return null;
        }

        return toEntity(userRequest.title(), userRequest.description(), client, null);
    }

    public com.thiagsilvadev.helpdesk.entity.Ticket toEntityFromStaffRequest(CreateStaffTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return toEntity(request.title(), request.description(), client, request.priority());
    }

    private com.thiagsilvadev.helpdesk.entity.Ticket toEntity(String title, String description, User client, TicketPriority priority) {
        return new com.thiagsilvadev.helpdesk.entity.Ticket(title, description, client, priority);
    }

    private TicketUserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return new TicketUserInfo(
                user.getId(),
                user.getName()
        );
    }
}
