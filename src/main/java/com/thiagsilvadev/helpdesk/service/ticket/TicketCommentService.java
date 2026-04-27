package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.TicketCommentDTO;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketComment;
import com.thiagsilvadev.helpdesk.entity.User;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketCommentMapper;
import com.thiagsilvadev.helpdesk.repository.TicketCommentRepository;
import com.thiagsilvadev.helpdesk.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;
    private final TicketQueryService ticketQueryService;
    private final UserService userService;
    private final TicketCommentMapper ticketCommentMapper;

    public TicketCommentService(TicketCommentRepository ticketCommentRepository,
                                TicketQueryService ticketQueryService,
                                UserService userService,
                                TicketCommentMapper ticketCommentMapper) {
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketQueryService = ticketQueryService;
        this.userService = userService;
        this.ticketCommentMapper = ticketCommentMapper;
    }

    @PreAuthorize("@ticketAuthorization.canReadAsParticipant(#ticketId, authentication)")
    public Page<TicketCommentDTO.Response> findByTicketId(Long ticketId, Pageable pageable) {
        return ticketCommentRepository.findByTicketId(ticketId, pageable)
                .map(ticketCommentMapper::toResponse);
    }

    @PreAuthorize("@ticketAuthorization.canReadAsParticipant(#ticketId, authentication)")
    @Transactional
    public TicketCommentDTO.Response create(Long ticketId, TicketCommentDTO.Create.Request request, Long authorId) {
        Ticket ticket = ticketQueryService.getTicketEntityById(ticketId);
        User author = userService.getUserById(authorId);
        TicketComment comment = ticketCommentMapper.toEntity(ticket, author, request.content());

        return ticketCommentMapper.toResponse(ticketCommentRepository.save(comment));
    }

    @PreAuthorize("@ticketAuthorization.canReadAsParticipant(#ticketId, authentication) "
            + "and @ticketCommentAuthorization.canModify(#commentId, authentication)")
    @Transactional
    public TicketCommentDTO.Response update(Long ticketId, Long commentId, TicketCommentDTO.Update.Request request) {
        TicketComment comment = getCommentEntityByIdAndTicketId(commentId, ticketId);
        comment.updateContent(request.content());

        return ticketCommentMapper.toResponse(ticketCommentRepository.save(comment));
    }

    @PreAuthorize("@ticketAuthorization.canReadAsParticipant(#ticketId, authentication) "
            + "and @ticketCommentAuthorization.canModify(#commentId, authentication)")
    @Transactional
    public void delete(Long ticketId, Long commentId) {
        TicketComment comment = getCommentEntityByIdAndTicketId(commentId, ticketId);
        ticketCommentRepository.delete(comment);
    }

    private TicketComment getCommentEntityByIdAndTicketId(Long commentId, Long ticketId) {
        return ticketCommentRepository.findByIdAndTicketId(commentId, ticketId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
    }
}
