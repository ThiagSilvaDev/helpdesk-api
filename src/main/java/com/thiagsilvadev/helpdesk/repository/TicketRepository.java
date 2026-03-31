package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	boolean existsByIdAndClientEmail(Long id, String email);

	boolean existsByIdAndTechnicianEmail(Long id, String email);
}
