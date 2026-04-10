package com.thiagsilvadev.helpdesk.repository;

import com.thiagsilvadev.helpdesk.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

	boolean existsByIdAndClientId(Long id, Long clientId);

	boolean existsByIdAndTechnicianEmail(Long id, String email);

	Optional<Ticket> findByIdAndClientId(Long ticketId, Long userId);

	Page<Ticket> findByClientId(Long clientId, Pageable pageable);
}
