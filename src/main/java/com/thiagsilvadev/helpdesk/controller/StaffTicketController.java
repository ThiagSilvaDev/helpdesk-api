package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommandService;
import com.thiagsilvadev.helpdesk.service.ticket.TicketQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springdoc.core.annotations.ParameterObject;

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
    @Operation(summary = "Create ticket for requester", description = "Staff creates a ticket on behalf of a user, with explicit priority")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketDTO.Response> createTicketAsStaff(@RequestBody @Valid TicketDTO.Create.StaffRequest request) {
        TicketDTO.Response newTicket = ticketCommandService.createByStaff(request);
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
    @Operation(summary = "Get ticket by ID", description = "Returns a single ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.Response.class))),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketDTO.Response> getTicketByIdForStaff(@PathVariable Long ticketId) {
        TicketDTO.Response ticket = ticketQueryService.getTicketResponseById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List all tickets", description = "Returns a paginated, filterable list of all tickets (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved")})
    public ResponseEntity<Page<TicketDTO.Response>> listTicketsForStaff(@ParameterObject TicketDTO.Search.Criteria criteria,
                                                                               @ParameterObject Pageable pageable) {
        Page<TicketDTO.Response> tickets = ticketQueryService.findAll(criteria, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}/priority")
    @Operation(summary = "Update ticket priority", description = "Changes the priority of a ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Priority updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.Response.class))),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketDTO.Response> updateTicketPriorityAsStaff(@PathVariable Long ticketId, @RequestBody @Valid TicketDTO.UpdatePriority.Request request) {
        TicketDTO.Response updatedTicket = ticketCommandService.updatePriority(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/technician")
    @Operation(summary = "Assign technician", description = "Assigns a technician to a ticket and sets status to IN_PROGRESS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technician assigned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.Response.class))),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<TicketDTO.Response> assignTechnicianToTicket(@PathVariable Long ticketId,
                                                                              @RequestBody @Valid TicketDTO.AssignTechnician.Request request,
                                                                              @AuthenticationPrincipal Jwt jwt
    ) {
        TicketDTO.Response updatedTicket = ticketCommandService.assignTechnician(ticketId, request.technicianId(), authenticatedUserId(jwt));
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    @Operation(summary = "Close ticket", description = "Closes a ticket (admin, or assigned technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket closed"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<Void> closeTicketAsStaff(@PathVariable Long ticketId) {
        ticketCommandService.close(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ticketId}/cancel")
    @Operation(summary = "Cancel ticket", description = "Cancels a ticket (admin, technician, or ticket owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket cancelled"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound"),
            @ApiResponse(responseCode = "422", ref = "UnprocessableEntity")
    })
    public ResponseEntity<Void> cancelTicketAsStaff(@PathVariable Long ticketId) {
        ticketCommandService.cancel(ticketId);
        return ResponseEntity.noContent().build();
    }

    private Long authenticatedUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
