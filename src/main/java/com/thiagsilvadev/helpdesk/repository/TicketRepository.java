package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	boolean existsByIdAndClientId(Long id, Long clientId);

	boolean existsByIdAndTechnicianEmail(Long id, String email);
}
