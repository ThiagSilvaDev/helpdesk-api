package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.ticket.AssignTechnicianRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.CreateStaffTicketRequest;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.dto.ticket.TicketSearchCriteria;
import com.thiagsilvadev.helpdesk.dto.ticket.UpdateTicketPriorityRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(("/api/staff/tickets"))
@Tag(name = "Staff Tickets", description = "Ticket management for technicians and admins")
@SecurityRequirement(name = "bearerAuth")
public class StaffTicketController {

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    public StaffTicketController(TicketCommandService ticketCommandService, TicketQueryService ticketQueryService) {
        this.ticketCommandService = ticketCommandService;
        this.ticketQueryService = ticketQueryService;
    }

    @PostMapping
    @Operation(operationId = "createTicketAsStaff", summary = "Create ticket for requester", description = "Staff creates a ticket on behalf of a user, with explicit priority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created"),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
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

    @GetMapping("/{ticketId}")
    @Operation(operationId = "getTicketByIdForStaff", summary = "Get ticket by ID", description = "Returns a single ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketResponse> getTicketByIdForStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    ) {
        TicketResponse ticket = ticketQueryService.getTicketResponseById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "listTicketsForStaff", summary = "List all tickets", description = "Returns a paginated, filterable list of all tickets (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden")
    })
    public ResponseEntity<Page<TicketResponse>> listTicketsForStaff(@ParameterObject TicketSearchCriteria criteria,
                                                                               @ParameterObject Pageable pageable) {
        Page<TicketResponse> tickets = ticketQueryService.findAll(criteria, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}/priority")
    @Operation(operationId = "updateTicketPriorityAsStaff", summary = "Update ticket priority", description = "Changes the priority of a ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Priority updated"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketResponse> updateTicketPriorityAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
            @RequestBody @Valid UpdateTicketPriorityRequest request
    ) {
        TicketResponse updatedTicket = ticketCommandService.updatePriority(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/technician")
    @Operation(operationId = "assignTechnicianToTicket", summary = "Assign technician", description = "Assigns a technician to a ticket and sets status to IN_PROGRESS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technician assigned"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketResponse> assignTechnicianToTicket(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId,
                                                                        @RequestBody @Valid AssignTechnicianRequest request,
                                                                        @Parameter(hidden = true) @CurrentUserId Long userId
    ) {
        TicketResponse updatedTicket = ticketCommandService.assignTechnician(ticketId, request.technicianId(), userId);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    @Operation(operationId = "closeTicketAsStaff", summary = "Close ticket", description = "Closes a ticket (admin, or assigned technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket closed"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<Void> closeTicketAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    ) {
        ticketCommandService.close(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ticketId}/cancel")
    @Operation(operationId = "cancelTicketAsStaff", summary = "Cancel ticket", description = "Cancels a ticket (admin, technician, or ticket owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket cancelled"),
            @ApiResponse(responseCode = "401", ref = "Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<Void> cancelTicketAsStaff(
            @Parameter(description = "Ticket id", example = "100")
            @PathVariable Long ticketId
    ) {
        ticketCommandService.cancel(ticketId);
        return ResponseEntity.noContent().build();
    }
}
