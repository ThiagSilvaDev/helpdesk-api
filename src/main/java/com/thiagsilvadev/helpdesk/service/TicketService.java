package com.thiagsilvadev.helpdesk.service;

import com.thiagsilvadev.helpdesk.dto.ticket.CreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.mapper.TicketRequestMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final TicketMapper ticketMapper;
    private final TicketRequestMapper ticketRequestMapper;

    public TicketService(TicketRepository ticketRepository,
                         UserService userService,
                         TicketMapper ticketMapper,
                         TicketRequestMapper ticketRequestMapper) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.ticketMapper = ticketMapper;
        this.ticketRequestMapper = ticketRequestMapper;
    }

    @PreAuthorize("@ticketAuthorization.canCreate(authentication)")
    public TicketResponse create(CreateTicketRequest request, Long authenticatedUserId) {
        User client = userService.getUserById(authenticatedUserId);

        Ticket newTicket = ticketRequestMapper.toEntity(request, client);

        return ticketMapper.toResponse(ticketRepository.save(newTicket));
    }

    private Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public List<TicketResponse> findAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @PreAuthorize("@ticketAuthorization.canRead(#id, authentication)")
    public TicketResponse getTicketResponseById(Long id) {
        return ticketMapper.toResponse(getTicketById(id));
    }

    @PreAuthorize("@ticketAuthorization.canUpdate(#id, authentication)")
    public TicketResponse update(Long id, UpdateTicketRequest request) {
        Ticket existingTicket = getTicketById(id);

        ticketRequestMapper.applyUpdate(request, existingTicket);

        return ticketMapper.toResponse(ticketRepository.save(existingTicket));
    }

    @PreAuthorize("@ticketAuthorization.canAssignTechnician(#technicianId, authentication)")
    public TicketResponse assignTechnician(Long ticketId, Long technicianId) {
        Ticket existingTicket = getTicketById(ticketId);
        User technician = userService.getUserById(technicianId);

        existingTicket.setTechnician(technician);

        return ticketMapper.toResponse(ticketRepository.save(existingTicket));
    }

    @PreAuthorize("@ticketAuthorization.canClose(#id, authentication)")
    public void close(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.closeTicket();
        ticketRepository.save(existingTicket);
    }

    @PreAuthorize("@ticketAuthorization.canCancel(#id, authentication)")
    public void cancel(Long id) {
        Ticket existingTicket = getTicketById(id);
        existingTicket.cancelTicket();
        ticketRepository.save(existingTicket);
    }
}
