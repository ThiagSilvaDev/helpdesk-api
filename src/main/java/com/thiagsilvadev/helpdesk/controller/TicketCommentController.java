package com.thiagsilvadev.helpdesk.controller;

import com.thiagsilvadev.helpdesk.api.TicketCommentApi;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.CreateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentResponse;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.UpdateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.security.CurrentUserId;
import com.thiagsilvadev.helpdesk.service.ticket.TicketCommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
public class TicketCommentController implements TicketCommentApi {

    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService) {
        this.ticketCommentService = ticketCommentService;
    }

    @Override
    public ResponseEntity<TicketCommentResponse> createTicketComment(
            @PathVariable Long ticketId,
            @RequestBody @Valid CreateTicketCommentRequest request,
            @CurrentUserId Long userId) {
        TicketCommentResponse comment = ticketCommentService.create(ticketId, request, userId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{commentId}")
                .buildAndExpand(comment.id())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(comment);
    }

    @Override
    public ResponseEntity<Page<TicketCommentResponse>> listTicketComments(
            @PathVariable Long ticketId,
            Pageable pageable) {
        return ResponseEntity.ok(ticketCommentService.findByTicketId(ticketId, pageable));
    }

    @Override
    public ResponseEntity<TicketCommentResponse> updateTicketComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateTicketCommentRequest request) {
        return ResponseEntity.ok(ticketCommentService.update(ticketId, commentId, request));
    }

    @Override
    public ResponseEntity<Void> deleteTicketComment(
            @PathVariable Long ticketId,
            @PathVariable Long commentId
    ) {
        ticketCommentService.delete(ticketId, commentId);
        return ResponseEntity.noContent().build();
    }
}
