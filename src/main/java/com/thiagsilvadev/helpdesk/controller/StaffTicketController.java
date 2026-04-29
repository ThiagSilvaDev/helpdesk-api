package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.StaffTicketApi;
import com.thiagsilvadev.helpdesk.dto.ticket.AssignTechnicianRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketSearchCriteria;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketPriorityRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class StaffTicketController implements StaffTicketApi {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public StaffTicketController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @Override
    public ResponseEntity<TicketResponse> createTicketAsStaff(@RequestBody @Valid CreateStaffTicketRequest request) {
        TicketResponse newTicket = ticketCommandService.createByStaff(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTicket.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(newTicket);
    }

    @Override
    public ResponseEntity<TicketResponse> getTicketByIdForStaff(
            @PathVariable Long ticketId
    ) {
        TicketResponse ticket = ticketQueryService.getTicketResponseById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @Override
    public ResponseEntity<Page<TicketResponse>> listTicketsForStaff(TicketSearchCriteria criteria, Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findAll(criteria, pageable);
        return ResponseEntity.ok(tickets);
    }

    @Override
    public ResponseEntity<TicketResponse> updateTicketPriorityAsStaff(
            @PathVariable Long ticketId,
            @RequestBody @Valid UpdateTicketPriorityRequest request
    ) {
        TicketResponse updatedTicket = ticketCommandService.updatePriority(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @Override
    public ResponseEntity<TicketResponse> assignTechnicianToTicket(
            @PathVariable Long ticketId,
            @RequestBody @Valid AssignTechnicianRequest request,
            @CurrentUserId Long userId
    ) {
        TicketResponse updatedTicket = ticketCommandService.assignTechnician(ticketId, request.technicianId(), userId);
        return ResponseEntity.ok(updatedTicket);
    }

    @Override
    public ResponseEntity<Void> closeTicketAsStaff(
            @PathVariable Long ticketId
    ) {
        ticketCommandService.close(ticketId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> cancelTicketAsStaff(
            @PathVariable Long ticketId
    ) {
        ticketCommandService.cancel(ticketId);
        return ResponseEntity.noContent().build();
    }
}
