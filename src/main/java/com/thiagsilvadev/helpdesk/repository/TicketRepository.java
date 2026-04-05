package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.dto.ticket.TicketResponse;
import com.thiagsilvadev.helpdesk.entity.Ticket;
import com.thiagsilvadev.helpdesk.entity.TicketPriority;
import com.thiagsilvadev.helpdesk.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	boolean existsByIdAndClientId(Long id, Long clientId);

	boolean existsByIdAndTechnicianEmail(Long id, String email);
}
