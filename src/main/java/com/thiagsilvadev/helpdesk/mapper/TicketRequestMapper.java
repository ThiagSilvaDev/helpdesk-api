package com.thiagsilvadev.helpdesk.mapper;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TicketRequestMapper {

    public Ticket toEntity(CreateTicketRequest request, User client) {
        if (request == null || client == null) {
            return null;
        }

        return new Ticket(request.title(), request.description(), client);
    }

    public void applyUpdate(UpdateTicketRequest request, Ticket ticket) {
        if (request == null || ticket == null) {
            return;
        }

        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
    }
}

