package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketDTO.Response toResponse(com.thiagsilvadev.helpdesk.entity.Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return new TicketDTO.Response(
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

    public com.thiagsilvadev.helpdesk.entity.Ticket toEntityFromUserRequest(TicketDTO.Create.UserRequest userRequest, User client) {
        if (userRequest == null || client == null) {
            return null;
        }

        return toEntity(userRequest.title(), userRequest.description(), client, null);
    }

    public com.thiagsilvadev.helpdesk.entity.Ticket toEntityFromStaffRequest(TicketDTO.Create.StaffRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return toEntity(request.title(), request.description(), client, request.priority());
    }

    private com.thiagsilvadev.helpdesk.entity.Ticket toEntity(String title, String description, User client, TicketPriority priority) {
        return new com.thiagsilvadev.helpdesk.entity.Ticket(title, description, client, priority);
    }

    private TicketDTO.Response.UserInfo toUserInfo(User user) {
        if (user == null) {
            return null;
        }

        return new TicketDTO.Response.UserInfo(
                user.getId(),
                user.getName()
        );
    }
}
