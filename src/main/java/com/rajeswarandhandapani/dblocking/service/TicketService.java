package com.rajeswarandhandapani.dblocking.service;

import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket createTicket(Ticket ticket) {
        logger.info("Creating new ticket: {}", ticket.getName());
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Book a ticket using pessimistic locking.
     * This method demonstrates how pessimistic locking prevents concurrent bookings.
     */
    @Transactional
    public Ticket bookTicket(Long id) throws InterruptedException {
        logger.info("Attempting to book ticket with ID: {}", id);
        
        // Use pessimistic locking to prevent concurrent access
        Optional<Ticket> optionalTicket = ticketRepository.findByIdWithLock(id);
        
        if (optionalTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found with ID: " + id);
        }
        
        Ticket ticket = optionalTicket.get();
        
        if (ticket.isBooked()) {
            logger.warn("Ticket {} is already booked", id);
            throw new IllegalStateException("Ticket is already booked");
        }
        
        // Simulate some processing time
        Thread.sleep(2000);
        
        ticket.setBooked(true);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        logger.info("Successfully booked ticket: {}", savedTicket);
        return savedTicket;
    }

    /**
     * Cancel a ticket booking
     */
    @Transactional
    public Ticket cancelBooking(Long id) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        
        if (optionalTicket.isEmpty()) {
            throw new IllegalArgumentException("Ticket not found with ID: " + id);
        }
        
        Ticket ticket = optionalTicket.get();
        ticket.setBooked(false);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        logger.info("Cancelled booking for ticket: {}", savedTicket);
        return savedTicket;
    }
}
