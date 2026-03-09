package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.AssignTechnicianRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketRequest;
import com.thiagsilvadev.helpdesk.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody CreateTicketRequest request) {
        TicketResponse newTicket = ticketService.create(request);
        return ResponseEntity.ok(newTicket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable Long id) {
        TicketResponse ticket = TicketResponse
                .fromEntity(ticketService.getTicketById(id));
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> findAll() {
        List<TicketResponse> tickets = ticketService.findAll();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody UpdateTicketRequest request) {
        TicketResponse updatedTicket = ticketService.update(id, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{id}/technician")
    public ResponseEntity<TicketResponse> assignTechnician(@PathVariable Long id, @RequestBody AssignTechnicianRequest request) {
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
