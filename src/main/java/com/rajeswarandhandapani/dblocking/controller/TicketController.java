package com.rajeswarandhandapani.dblocking.controller;

import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @PostMapping
    public Ticket createTicket(@RequestBody Ticket ticket) {
        return ticketService.createTicket(ticket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Book a ticket using pessimistic locking.
     * This endpoint demonstrates how pessimistic locking prevents concurrent bookings.
     */
    @PostMapping("/{id}/book")
    public ResponseEntity<Ticket> bookTicket(@PathVariable Long id) throws InterruptedException {
        Ticket bookedTicket = ticketService.bookTicket(id);
        return ResponseEntity.ok(bookedTicket);
    }

    /**
     * Cancel a ticket booking
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Ticket> cancelBooking(@PathVariable Long id) {
        Ticket cancelledTicket = ticketService.cancelBooking(id);
        return ResponseEntity.ok(cancelledTicket);
    }
}
