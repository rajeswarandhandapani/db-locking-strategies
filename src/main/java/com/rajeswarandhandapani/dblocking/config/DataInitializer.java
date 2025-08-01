package com.rajeswarandhandapani.dblocking.config;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import com.rajeswarandhandapani.dblocking.model.Ticket;
import com.rajeswarandhandapani.dblocking.repository.InventoryItemRepository;
import com.rajeswarandhandapani.dblocking.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing sample data...");

        // Create sample tickets for pessimistic locking demo
        if (ticketRepository.count() == 0) {
            Ticket ticket1 = new Ticket();
            ticket1.setName("Concert - Taylor Swift");
            ticket1.setBooked(false);
            ticketRepository.save(ticket1);

            Ticket ticket2 = new Ticket();
            ticket2.setName("Football Match - Arsenal vs Chelsea");
            ticket2.setBooked(false);
            ticketRepository.save(ticket2);

            Ticket ticket3 = new Ticket();
            ticket3.setName("Theater - Hamilton");
            ticket3.setBooked(true);
            ticketRepository.save(ticket3);

            logger.info("Created {} sample tickets", 3);
        }

        // Create sample inventory items for optimistic locking demo
        if (inventoryItemRepository.count() == 0) {
            InventoryItem item1 = new InventoryItem();
            item1.setName("iPhone 15");
            item1.setQuantity(25);
            inventoryItemRepository.save(item1);

            InventoryItem item2 = new InventoryItem();
            item2.setName("MacBook Pro");
            item2.setQuantity(10);
            inventoryItemRepository.save(item2);

            InventoryItem item3 = new InventoryItem();
            item3.setName("AirPods Pro");
            item3.setQuantity(50);
            inventoryItemRepository.save(item3);

            logger.info("Created {} sample inventory items", 3);
        }

        logger.info("Data initialization completed!");
    }
}
