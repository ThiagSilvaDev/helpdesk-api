package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.ticketcomment.CreateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.TicketCommentResponse;
import com.thiagsilvadev.helpdesk.dto.ticketcomment.UpdateTicketCommentRequest;
import com.thiagsilvadev.helpdesk.entity.*;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketCommentMapper;
import com.thiagsilvadev.helpdesk.repository.TicketCommentRepository;
import com.thiagsilvadev.helpdesk.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    private static final Long TICKET_ID = 100L;
    private static final Long COMMENT_ID = 200L;
    private static final Long AUTHOR_ID = 42L;

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @Mock
    private TicketQueryService ticketQueryService;

    @Mock
    private UserService userService;

    @Spy
    private TicketCommentMapper ticketCommentMapper = new TicketCommentMapper();

    @InjectMocks
    private TicketCommentService ticketCommentService;

    @Test
    void shouldListCommentsByTicketId() {
        PageRequest pageable = PageRequest.of(0, 10);
        given(ticketCommentRepository.findByTicketId(TICKET_ID, pageable))
                .willReturn(new PageImpl<>(List.of(comment("Existing comment")), pageable, 1));

        Page<TicketCommentResponse> response = ticketCommentService.findByTicketId(TICKET_ID, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().ticketId()).isEqualTo(TICKET_ID);
    }

    @Test
    void shouldCreateComment() {
        Ticket ticket = ticket();
        User author = user(AUTHOR_ID, Roles.ROLE_USER);
        CreateTicketCommentRequest request = new CreateTicketCommentRequest("Need help with this ticket");

        given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
        given(userService.getUserById(AUTHOR_ID)).willReturn(author);
        given(ticketCommentRepository.save(any(TicketComment.class)))
                .willAnswer(invocation -> persistComment(invocation.getArgument(0), COMMENT_ID));

        TicketCommentResponse response = ticketCommentService.create(TICKET_ID, request, AUTHOR_ID);

        assertThat(response.id()).isEqualTo(COMMENT_ID);
        assertThat(response.content()).isEqualTo("Need help with this ticket");
        assertThat(response.author().id()).isEqualTo(AUTHOR_ID);
    }

    @Test
    void shouldUpdateComment() {
        TicketComment comment = comment("Old content");
        UpdateTicketCommentRequest request = new UpdateTicketCommentRequest("Updated content");
        given(ticketCommentRepository.findByIdAndTicketId(COMMENT_ID, TICKET_ID)).willReturn(Optional.of(comment));
        given(ticketCommentRepository.save(comment)).willReturn(comment);

        TicketCommentResponse response = ticketCommentService.update(TICKET_ID, COMMENT_ID, request);

        assertThat(response.content()).isEqualTo("Updated content");
    }

    @Test
    void shouldDeleteComment() {
        TicketComment comment = comment("Comment to delete");
        given(ticketCommentRepository.findByIdAndTicketId(COMMENT_ID, TICKET_ID)).willReturn(Optional.of(comment));

        ticketCommentService.delete(TICKET_ID, COMMENT_ID);

        then(ticketCommentRepository).should().delete(comment);
    }

    @Test
    void shouldThrowWhenCommentIsMissing() {
        given(ticketCommentRepository.findByIdAndTicketId(COMMENT_ID, TICKET_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> ticketCommentService.update(
                        TICKET_ID,
                        COMMENT_ID,
                        new UpdateTicketCommentRequest("Updated content")
                ))
                .withMessage("Comment not found with id: " + COMMENT_ID);
    }

    private TicketComment comment(String content) {
        return persistComment(new TicketComment(ticket(), user(AUTHOR_ID, Roles.ROLE_USER), content), COMMENT_ID);
    }

    private TicketComment persistComment(TicketComment comment, Long id) {
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    private Ticket ticket() {
        Ticket ticket = new Ticket("Printer issue", "Office printer is not working", user(10L, Roles.ROLE_USER), TicketPriority.TRIAGE);
        ReflectionTestUtils.setField(ticket, "id", TICKET_ID);
        return ticket;
    }

    private User user(Long id, Roles role) {
        User user = new User(role.name(), role.name().toLowerCase() + "@helpdesk.local", "encoded-password", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
