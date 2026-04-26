package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.entity.*;
import com.thiagsilvadev.helpdesk.exception.InvalidRoleAssignmentException;
import com.thiagsilvadev.helpdesk.exception.InvalidTicketStateException;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TicketCommandServiceTest {

    private static final Long TICKET_ID = 1L;
    private static final Long CLIENT_ID = 10L;
    private static final Long TECHNICIAN_ID = 20L;
    private static final Long AUTHENTICATED_USER_ID = 30L;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private TicketQueryService ticketQueryService;

    @Spy
    private TicketMapper ticketMapper = new TicketMapper();

    @InjectMocks
    private TicketCommandService ticketCommandService;

    @Nested
    class Create {

        @Test
        void shouldCreateByUserWithTriagePriority() {
            TicketDTO.Create.UserRequest request = new TicketDTO.Create.UserRequest(
                    "Printer issue",
                    "Office printer is not working"
            );
            User client = user(CLIENT_ID, Roles.ROLE_USER);

            given(userService.getUserById(CLIENT_ID)).willReturn(client);
            given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> persistTicket(invocation.getArgument(0), TICKET_ID));

            TicketDTO.Response response = ticketCommandService.createByUser(request, CLIENT_ID);

            assertThat(response.id()).isEqualTo(TICKET_ID);
            assertThat(response.title()).isEqualTo("Printer issue");
            assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
            assertThat(response.priority()).isEqualTo(TicketPriority.TRIAGE);
            assertThat(response.client().id()).isEqualTo(CLIENT_ID);
        }

        @Test
        void shouldCreateByStaffWithRequestedPriority() {
            TicketDTO.Create.StaffRequest request = new TicketDTO.Create.StaffRequest(
                    "VPN issue",
                    "Cannot connect to corporate VPN",
                    CLIENT_ID,
                    TicketPriority.HIGH
            );
            User client = user(CLIENT_ID, Roles.ROLE_USER);

            given(userService.getUserById(CLIENT_ID)).willReturn(client);
            given(ticketRepository.save(any(Ticket.class))).willAnswer(invocation -> persistTicket(invocation.getArgument(0), TICKET_ID));

            TicketDTO.Response response = ticketCommandService.createByStaff(request);

            assertThat(response.priority()).isEqualTo(TicketPriority.HIGH);
            assertThat(response.client().id()).isEqualTo(CLIENT_ID);
        }

        @Test
        void shouldThrowWhenRequesterIsNotRoleUser() {
            TicketDTO.Create.UserRequest request = new TicketDTO.Create.UserRequest(
                    "Printer issue",
                    "Office printer is not working"
            );
            given(userService.getUserById(CLIENT_ID)).willReturn(user(CLIENT_ID, Roles.ROLE_ADMIN));

            assertThatExceptionOfType(InvalidRoleAssignmentException.class)
                    .isThrownBy(() -> ticketCommandService.createByUser(request, CLIENT_ID))
                    .withMessage("Only users with ROLE_USER can open a ticket");

            then(ticketRepository).should(never()).save(any());
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateTitleAndDescription() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);
            TicketDTO.Update.Request request = new TicketDTO.Update.Request("New title", "New description for ticket");

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            TicketDTO.Response response = ticketCommandService.update(TICKET_ID, request);

            assertThat(response.title()).isEqualTo("New title");
            assertThat(response.description()).isEqualTo("New description for ticket");
        }

        @Test
        void shouldRejectUpdatesToClosedTickets() {
            Ticket ticket = ticket("Closed ticket", "Closed description", TicketPriority.TRIAGE);
            ticket.closeTicket();
            TicketDTO.Update.Request request = new TicketDTO.Update.Request("New title", "New description for ticket");

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);

            assertThatExceptionOfType(InvalidTicketStateException.class)
                    .isThrownBy(() -> ticketCommandService.update(TICKET_ID, request))
                    .withMessage("Cannot update a CLOSED ticket");

            then(ticketRepository).should(never()).save(any());
        }
    }

    @Nested
    class UpdatePriority {

        @Test
        void shouldUpdatePriority() {
            Ticket ticket = ticket("Priority issue", "Need urgent change now", TicketPriority.TRIAGE);
            TicketDTO.UpdatePriority.Request request = new TicketDTO.UpdatePriority.Request(TicketPriority.URGENT);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            TicketDTO.Response response = ticketCommandService.updatePriority(TICKET_ID, request);

            assertThat(response.priority()).isEqualTo(TicketPriority.URGENT);
        }

        @Test
        void shouldRejectPriorityChangesForClosedTickets() {
            Ticket ticket = ticket("Closed ticket", "Ticket already closed state", TicketPriority.TRIAGE);
            ticket.closeTicket();
            TicketDTO.UpdatePriority.Request request = new TicketDTO.UpdatePriority.Request(TicketPriority.HIGH);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);

            assertThatExceptionOfType(InvalidTicketStateException.class)
                    .isThrownBy(() -> ticketCommandService.updatePriority(TICKET_ID, request))
                    .withMessage("Cannot change priority of a closed ticket");

            then(ticketRepository).should(never()).save(any());
        }
    }

    @Nested
    class AssignTechnician {

        @Test
        void shouldAssignTechnicianAndMoveTicketToInProgress() {
            Ticket ticket = ticket("Wi-Fi issue", "Intermittent connection in office", TicketPriority.TRIAGE);
            User technician = user(TECHNICIAN_ID, Roles.ROLE_TECHNICIAN);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(userService.getUserById(TECHNICIAN_ID)).willReturn(technician);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            TicketDTO.Response response = ticketCommandService.assignTechnician(TICKET_ID, TECHNICIAN_ID, AUTHENTICATED_USER_ID);

            assertThat(response.technician().id()).isEqualTo(TECHNICIAN_ID);
            assertThat(response.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        }

        @Test
        void shouldUseAuthenticatedUserWhenTechnicianIdIsNull() {
            Ticket ticket = ticket("Network issue", "Cannot reach internal systems", TicketPriority.TRIAGE);
            User technician = user(AUTHENTICATED_USER_ID, Roles.ROLE_TECHNICIAN);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(userService.getUserById(AUTHENTICATED_USER_ID)).willReturn(technician);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            ticketCommandService.assignTechnician(TICKET_ID, null, AUTHENTICATED_USER_ID);

            then(userService).should().getUserById(AUTHENTICATED_USER_ID);
        }

        @Test
        void shouldRejectNonTechnicianAssignee() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(userService.getUserById(TECHNICIAN_ID)).willReturn(user(TECHNICIAN_ID, Roles.ROLE_USER));

            assertThatExceptionOfType(InvalidRoleAssignmentException.class)
                    .isThrownBy(() -> ticketCommandService.assignTechnician(TICKET_ID, TECHNICIAN_ID, AUTHENTICATED_USER_ID))
                    .withMessage("Assigned user must have TECHNICIAN role");

            then(ticketRepository).should(never()).save(any());
        }
    }

    @Nested
    class CloseAndCancel {

        @Test
        void shouldCloseTicket() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            ticketCommandService.close(TICKET_ID);

            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CLOSED);
            assertThat(ticket.getClosedAt()).isNotNull();
        }

        @Test
        void shouldCancelTicket() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);
            given(ticketRepository.save(ticket)).willReturn(ticket);

            ticketCommandService.cancel(TICKET_ID);

            assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        }

        @Test
        void shouldRejectClosingCancelledTicket() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);
            ticket.cancelTicket();

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);

            assertThatExceptionOfType(InvalidTicketStateException.class)
                    .isThrownBy(() -> ticketCommandService.close(TICKET_ID))
                    .withMessage("Cannot close a cancelled ticket");

            then(ticketRepository).should(never()).save(any());
        }

        @Test
        void shouldRejectCancellingClosedTicket() {
            Ticket ticket = ticket("Old title", "Old description for ticket", TicketPriority.TRIAGE);
            ticket.closeTicket();

            given(ticketQueryService.getTicketEntityById(TICKET_ID)).willReturn(ticket);

            assertThatExceptionOfType(InvalidTicketStateException.class)
                    .isThrownBy(() -> ticketCommandService.cancel(TICKET_ID))
                    .withMessage("Cannot cancel a closed ticket");

            then(ticketRepository).should(never()).save(any());
        }
    }

    private Ticket ticket(String title, String description, TicketPriority priority) {
        Ticket ticket = new Ticket(title, description, user(CLIENT_ID, Roles.ROLE_USER), priority);
        return persistTicket(ticket, TICKET_ID);
    }

    private Ticket persistTicket(Ticket ticket, Long id) {
        ReflectionTestUtils.setField(ticket, "id", id);
        return ticket;
    }

    private User user(Long id, Roles role) {
        User user = new User(role.name(), role.name().toLowerCase() + "@helpdesk.local", "encoded-password", role);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
