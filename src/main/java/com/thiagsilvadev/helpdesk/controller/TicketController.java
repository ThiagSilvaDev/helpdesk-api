package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> create(@RequestBody JsonNode body) {
        String title = body.get("title").asString();
        String description = body.get("description").asString();
        Long clientId = body.get("clientId").asLong();

        Ticket newTicket = ticketService.create(title, description, clientId);
        return ResponseEntity.ok(newTicket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> findAll() {
        List<Ticket> tickets = ticketService.findAll();
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> update(@PathVariable Long id, @RequestBody Ticket ticket) {
        Ticket updatedTicket = ticketService.update(id, ticket);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{id}/technician")
    public ResponseEntity<Ticket> assignTechnician(@PathVariable Long id, @RequestBody Long technicianId) {
        Ticket updatedTicket = ticketService.assignTechnician(id, technicianId);
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
