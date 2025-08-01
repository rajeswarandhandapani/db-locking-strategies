package com.rajeswarandhandapani.dblocking.controller;

import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        logger.info("Creating new ticket: {}", ticket.getName());
        return ticketRepository.save(ticket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketRepository.findById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Book a ticket using pessimistic locking.
     * This endpoint demonstrates how pessimistic locking prevents concurrent bookings.
     */
    @PostMapping("/{id}/book")
    @Transactional
    public ResponseEntity<?> bookTicket(@PathVariable Long id) throws InterruptedException {
        logger.info("Attempting to book ticket with ID: {}", id);
        
        // Use pessimistic locking to prevent concurrent access
        Optional<Ticket> optionalTicket = ticketRepository.findByIdWithLock(id);
        
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = optionalTicket.get();
        
        if (ticket.isBooked()) {
            logger.warn("Ticket {} is already booked", id);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Ticket is already booked");
        }
        
        // Simulate some processing time
        Thread.sleep(2000);
        
        ticket.setBooked(true);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        logger.info("Successfully booked ticket: {}", savedTicket);
        return ResponseEntity.ok(savedTicket);
    }

    /**
     * Cancel a ticket booking
     */
    @PostMapping("/{id}/cancel")
    @Transactional
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(id);
        
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Ticket ticket = optionalTicket.get();
        ticket.setBooked(false);
        Ticket savedTicket = ticketRepository.save(ticket);
        
        logger.info("Cancelled booking for ticket: {}", savedTicket);
        return ResponseEntity.ok(savedTicket);
    }
}
