package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.*;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid CreateTicketRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication is required");
        }

        TicketResponse newTicket = ticketService.create(request, principal.getId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newTicket.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(newTicket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketResponseById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll() {
        List<TicketResponse> tickets = ticketService.findAll();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateTicketRequest request) {
        TicketResponse updatedTicket = ticketService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<TicketResponse> updatePriority(@PathVariable Long id, @RequestBody @Valid UpdatePriorityRequest request) {
        TicketResponse updatedTicket = ticketService.updatePriority(id, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{id}/technician")
    public ResponseEntity<TicketResponse> assignTechnician(@PathVariable Long id, @RequestBody @Valid AssignTechnicianRequest request) {
        TicketResponse updatedTicket = ticketService
                .assignTechnician(id, request.technicianId());
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<Void> close(@PathVariable Long id) {
        ticketService.close(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        ticketService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
