package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.dto.TicketCommentDTO;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@Tag(name = "Ticket Comments", description = "Comments attached to tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketCommentController {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService) {
        this.ticketCommentService = ticketCommentService;
    }

    @PostMapping
    @Operation(summary = "Create ticket comment", description = "Adds a comment to a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketCommentDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketCommentDTO.Response> createTicketComment(@PathVariable Long ticketId,
                                                                         @RequestBody @Valid TicketCommentDTO.Create.Request request,
                                                                         @AuthenticationPrincipal Jwt jwt) {
        TicketCommentDTO.Response comment = ticketCommentService.create(ticketId, request, authenticatedUserId(jwt));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{commentId}")
                .buildAndExpand(comment.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(comment);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List ticket comments", description = "Returns a paginated list of comments for a ticket")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<Page<TicketCommentDTO.Response>> listTicketComments(@PathVariable Long ticketId,
                                                                              @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ticketCommentService.findByTicketId(ticketId, pageable));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update ticket comment", description = "Updates an existing ticket comment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketCommentDTO.Response.class))),
            @ApiResponse(responseCode = "400", ref = "BadRequest"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<TicketCommentDTO.Response> updateTicketComment(@PathVariable Long ticketId,
                                                                         @PathVariable Long commentId,
                                                                         @RequestBody @Valid TicketCommentDTO.Update.Request request) {
        return ResponseEntity.ok(ticketCommentService.update(ticketId, commentId, request));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete ticket comment", description = "Deletes an existing ticket comment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "403", ref = "Forbidden"),
            @ApiResponse(responseCode = "404", ref = "NotFound")
    })
    public ResponseEntity<Void> deleteTicketComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
        ticketCommentService.delete(ticketId, commentId);
        return ResponseEntity.noContent().build();
    }

    private Long authenticatedUserId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
