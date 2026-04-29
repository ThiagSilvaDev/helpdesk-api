package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.UserTicketApi;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateUserTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
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
public class UserTicketController implements UserTicketApi {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public UserTicketController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @Override
    public ResponseEntity<TicketResponse> createTicketAsUser(@RequestBody @Valid CreateUserTicketRequest userRequest,
                                                             @CurrentUserId Long userId) {
        TicketResponse newTicket = ticketCommandService.createByUser(userRequest, userId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTicket.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(newTicket);
    }

    @Override
    public ResponseEntity<TicketResponse> getTicketByIdForAuthenticatedUser(
            @PathVariable Long ticketId,
            @CurrentUserId Long userId) {
        TicketResponse ticket = ticketQueryService.getOwnTicketById(ticketId, userId);
        return ResponseEntity.ok(ticket);
    }

    @Override
    public ResponseEntity<Page<TicketResponse>> listAuthenticatedUserTickets(@CurrentUserId Long userId, Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findTicketsByClientId(userId, pageable);
        return ResponseEntity.ok(tickets);
    }

    @Override
    public ResponseEntity<TicketResponse> updateTicketAsUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTicketRequest request
    ) {
        TicketResponse updatedTicket = ticketCommandService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }
}
