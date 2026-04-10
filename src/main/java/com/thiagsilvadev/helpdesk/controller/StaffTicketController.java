package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.*;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(("/api/staff/tickets"))
public class StaffTicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public StaffTicketController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody @Valid StaffCreateTicketRequest request) {
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

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        TicketResponse ticket = ticketQueryService.getTicketResponseById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> findAll(TicketSearchCriteria criteria,
                                                        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findAll(criteria, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}/priority")
    public ResponseEntity<TicketResponse> updatePriority(@PathVariable Long ticketId, @RequestBody @Valid UpdatePriorityRequest request) {
        TicketResponse updatedTicket = ticketCommandService.updatePriority(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/technician")
    public ResponseEntity<TicketResponse> assignTechnician(@PathVariable Long ticketId,
                                                           @RequestBody @Valid AssignTechnicianRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal
    ) {
        TicketResponse updatedTicket = ticketCommandService.assignTechnician(ticketId, request.technicianId(), principal.getId());
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    public ResponseEntity<Void> close(@PathVariable Long ticketId) {
        ticketCommandService.close(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ticketId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long ticketId) {
        ticketCommandService.cancel(ticketId);
        return ResponseEntity.noContent().build();
    }
}
