package com.thiagsilvadev.helpdesk.service.ticket;

import com.thiagsilvadev.helpdesk.dto.TicketDTO;
import com.thiagsilvadev.helpdesk.exception.NotFoundException;
import com.thiagsilvadev.helpdesk.mapper.TicketMapper;
import com.thiagsilvadev.helpdesk.repository.TicketRepository;
import com.thiagsilvadev.helpdesk.repository.specification.TicketSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketQueryService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketQueryService(TicketRepository ticketRepository, TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    protected com.thiagsilvadev.helpdesk.entity.Ticket getTicketEntityById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + ticketId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public TicketDTO.Response getTicketResponseById(Long ticketId) {
        return ticketMapper.toResponse(getTicketEntityById(ticketId));
    }

    @PreAuthorize("@ticketAuthorization.canRead(#ticketId, authentication)")
    public TicketDTO.Response getOwnTicketById(Long ticketId, Long userId) {
        return ticketRepository.findByIdAndClientId(ticketId, userId)
                .map(ticketMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Ticket not found with id: " + ticketId));
    }

    public Page<TicketDTO.Response> findTicketsByClientId(Long clientId, Pageable pageable) {
        return ticketRepository.findByClientId(clientId, pageable)
                .map(ticketMapper::toResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public Page<TicketDTO.Response> findAll(TicketDTO.Search.Criteria criteria, Pageable pageable) {
        Specification<com.thiagsilvadev.helpdesk.entity.Ticket> spec = TicketSpecification.withCriteria(criteria);
        return ticketRepository.findAll(spec, pageable)
                .map(ticketMapper::toResponse);
    }
}
