package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.entity.*;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TicketQueryServiceTest {

    private static final Long TICKET_ID = 1L;
    private static final Long CLIENT_ID = 10L;

    @Mock
    private TicketRepository ticketRepository;

    @Spy
    private TicketMapper ticketMapper = new TicketMapper();

    @InjectMocks
    private TicketQueryService ticketQueryService;

    @Nested
    class GetTicketEntityById {

        @Test
        void shouldReturnTicketWhenFound() {
            Ticket ticket = ticket(TICKET_ID, TicketPriority.TRIAGE);
            given(ticketRepository.findById(TICKET_ID)).willReturn(Optional.of(ticket));

            Ticket found = ticketQueryService.getTicketEntityById(TICKET_ID);

            assertThat(found).isSameAs(ticket);
        }

        @Test
        void shouldThrowWhenMissing() {
            given(ticketRepository.findById(TICKET_ID)).willReturn(Optional.empty());

            assertThatExceptionOfType(NotFoundException.class)
                    .isThrownBy(() -> ticketQueryService.getTicketEntityById(TICKET_ID))
                    .withMessage("Ticket not found with id: " + TICKET_ID);
        }
    }

    @Test
    void shouldReturnTicketResponseById() {
        given(ticketRepository.findById(TICKET_ID)).willReturn(Optional.of(ticket(TICKET_ID, TicketPriority.HIGH)));

        TicketDTO.Response response = ticketQueryService.getTicketResponseById(TICKET_ID);

        assertThat(response.id()).isEqualTo(TICKET_ID);
        assertThat(response.priority()).isEqualTo(TicketPriority.HIGH);
    }

    @Test
    void shouldReturnOwnTicketById() {
        given(ticketRepository.findByIdAndClientId(TICKET_ID, CLIENT_ID))
                .willReturn(Optional.of(ticket(TICKET_ID, TicketPriority.TRIAGE)));

        TicketDTO.Response response = ticketQueryService.getOwnTicketById(TICKET_ID, CLIENT_ID);

        assertThat(response.id()).isEqualTo(TICKET_ID);
        assertThat(response.client().id()).isEqualTo(CLIENT_ID);
    }

    @Test
    void shouldThrowWhenOwnTicketIsMissing() {
        given(ticketRepository.findByIdAndClientId(TICKET_ID, CLIENT_ID)).willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> ticketQueryService.getOwnTicketById(TICKET_ID, CLIENT_ID))
                .withMessage("Ticket not found with id: " + TICKET_ID);
    }

    @Test
    void shouldFindTicketsByClientId() {
        PageRequest pageable = PageRequest.of(0, 20);
        given(ticketRepository.findByClientId(CLIENT_ID, pageable))
                .willReturn(new PageImpl<>(List.of(ticket(TICKET_ID, TicketPriority.TRIAGE)), pageable, 1));

        Page<TicketDTO.Response> response = ticketQueryService.findTicketsByClientId(CLIENT_ID, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().getFirst().client().id()).isEqualTo(CLIENT_ID);
    }

    @Test
    void shouldFindAllWithCriteriaSpecification() {
        PageRequest pageable = PageRequest.of(0, 10);
        TicketDTO.Search.Criteria criteria = new TicketDTO.Search.Criteria(TicketStatus.OPEN, TicketPriority.URGENT);
        given(ticketRepository.findAll(anyTicketSpecification(), eq(pageable)))
                .willReturn(new PageImpl<>(List.of(ticket(TICKET_ID, TicketPriority.URGENT)), pageable, 1));

        Page<TicketDTO.Response> response = ticketQueryService.findAll(criteria, pageable);

        assertThat(response.getContent().getFirst().priority()).isEqualTo(TicketPriority.URGENT);
    }

    private Ticket ticket(Long id, TicketPriority priority) {
        User client = new User("Jane User", "jane@helpdesk.local", "encoded-password", Roles.ROLE_USER);
        ReflectionTestUtils.setField(client, "id", CLIENT_ID);
        Ticket ticket = new Ticket("Printer issue", "Office printer is not working", client, priority);
        ReflectionTestUtils.setField(ticket, "id", id);
        return ticket;
    }

    private Specification<Ticket> anyTicketSpecification() {
        return any();
    }
}
