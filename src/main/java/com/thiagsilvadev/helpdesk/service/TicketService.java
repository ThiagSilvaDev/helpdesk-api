package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;

    public TicketService(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    public TicketResponse create(CreateTicketRequest request) {
        User client = userService.getUserById(request.clientId());

        Ticket newTicket = new Ticket(request.title(), request.description(), client);

        return TicketResponse.fromEntity(ticketRepository.save(newTicket));
    }

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + id));
    }

    public List<TicketResponse> findAll() {
        return ticketRepository.findAll().stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    public TicketResponse update(Long id, UpdateTicketRequest request) {
        Ticket existingTicket = getTicketById(id);

        existingTicket.setTitle(request.title());
        existingTicket.setDescription(request.description());

        return TicketResponse.fromEntity(ticketRepository.save(existingTicket));
    }

    public TicketResponse assignTechnician(Long ticketId, Long technicianId) {
        Ticket existingTicket = getTicketById(ticketId);
        User technician = userService.getUserById(technicianId);

        existingTicket.setTechnician(technician);
        existingTicket.inProgressTicket();

        return TicketResponse.fromEntity(ticketRepository.save(existingTicket));
    }

    public void close(Long id) {
        Ticket existingTicket = getTicketById(id);

        if (existingTicket.getStatus() == TicketStatus.CLOSED) {
            throw new RuntimeException("Ticket is already closed with id: " + id);
        }
        existingTicket.closeTicket();

        ticketRepository.save(existingTicket);
    }

    public void cancel(Long id) {
        Ticket existingTicket = getTicketById(id);

        existingTicket.cancelTicket();

        ticketRepository.save(existingTicket);
    }
}
