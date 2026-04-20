package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketDto;
import com.thiagsilvadev.helpdesk.security.UserPrincipal;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Requester not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TicketDto.TicketResponse> create(@RequestBody @Valid TicketDto.StaffCreateTicketRequest request) {
        TicketDto.TicketResponse newTicket = ticketCommandService.createByStaff(request);
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
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TicketDto.TicketResponse> getTicketById(@PathVariable Long ticketId) {
        TicketDto.TicketResponse ticket = ticketQueryService.getTicketResponseById(ticketId);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping
    @Operation(summary = "List all tickets", description = "Returns a paginated, filterable list of all tickets (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<TicketDto.TicketResponse>> findAll(@ParameterObject TicketDto.TicketSearchCriteria criteria,
                                                        @ParameterObject @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<TicketDto.TicketResponse> tickets = ticketQueryService.findAll(criteria, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PatchMapping("/{ticketId}/priority")
    @Operation(summary = "Update ticket priority", description = "Changes the priority of a ticket (admin/technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Priority updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state — cannot change priority of closed ticket",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TicketDto.TicketResponse> updatePriority(@PathVariable Long ticketId, @RequestBody @Valid TicketDto.UpdatePriorityRequest request) {
        TicketDto.TicketResponse updatedTicket = ticketCommandService.updatePriority(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/technician")
    @Operation(summary = "Assign technician", description = "Assigns a technician to a ticket and sets status to IN_PROGRESS")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Technician assigned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDto.TicketResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ticket or technician not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state or role assignment",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TicketDto.TicketResponse> assignTechnician(@PathVariable Long ticketId,
                                                           @RequestBody @Valid TicketDto.AssignTechnicianRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal
    ) {
        TicketDto.TicketResponse updatedTicket = ticketCommandService.assignTechnician(ticketId, request.technicianId(), principal.getId());
        return ResponseEntity.ok(updatedTicket);
    }

    @PatchMapping("/{ticketId}/close")
    @Operation(summary = "Close ticket", description = "Closes a ticket (admin, or assigned technician)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket closed"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state — already closed or cancelled",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> close(@PathVariable Long ticketId) {
        ticketCommandService.close(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ticketId}/cancel")
    @Operation(summary = "Cancel ticket", description = "Cancels a ticket (admin, technician, or ticket owner)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ticket cancelled"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Invalid state — already closed or cancelled",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> cancel(@PathVariable Long ticketId) {
        ticketCommandService.cancel(ticketId);
        return ResponseEntity.noContent().build();
    }
}
