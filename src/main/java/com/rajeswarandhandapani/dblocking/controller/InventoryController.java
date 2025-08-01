package com.rajeswarandhandapani.dblocking.controller;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import com.rajeswarandhandapani.dblocking.repository.InventoryItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @GetMapping
    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    @PostMapping
    public InventoryItem createItem(@RequestBody InventoryItem item) {
        logger.info("Creating new inventory item: {}", item.getName());
        return inventoryItemRepository.save(item);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItem(@PathVariable Long id) {
        Optional<InventoryItem> item = inventoryItemRepository.findById(id);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update inventory quantity using optimistic locking.
     * This endpoint demonstrates how optimistic locking handles concurrent updates.
     */
    @PutMapping("/{id}/update-quantity")
    @Transactional
    public ResponseEntity<?> updateQuantity(@PathVariable Long id, @RequestParam int newQuantity) throws InterruptedException {
        logger.info("Attempting to update quantity for inventory item ID: {} to {}", id, newQuantity);
        
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(id);
        
        if (optionalItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        InventoryItem item = optionalItem.get();
        
        if (newQuantity < 0) {
            return ResponseEntity.badRequest()
                .body("Quantity cannot be negative");
        }
        
        // Simulate some processing time to increase chance of concurrent access
        Thread.sleep(5000);
        
        item.setQuantity(newQuantity);
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        logger.info("Successfully updated inventory item: {} to quantity {}", savedItem.getName(), savedItem.getQuantity());
        return ResponseEntity.ok(savedItem);
    }

    /**
     * Reduce inventory quantity (simulating a purchase)
     */
    @PostMapping("/{id}/reduce")
    @Transactional
    public ResponseEntity<?> reduceQuantity(@PathVariable Long id, @RequestParam int amount) throws InterruptedException {
        logger.info("Attempting to reduce quantity for inventory item ID: {} by {}", id, amount);
        
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(id);
        
        if (optionalItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        InventoryItem item = optionalItem.get();
        
        if (amount <= 0) {
            return ResponseEntity.badRequest()
                .body("Reduction amount must be positive");
        }
        
        if (item.getQuantity() < amount) {
            return ResponseEntity.badRequest()
                .body("Insufficient quantity available. Current: " + item.getQuantity() + ", Requested: " + amount);
        }
        
        // Simulate some processing time
        Thread.sleep(1000);
        
        item.setQuantity(item.getQuantity() - amount);
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        logger.info("Successfully reduced inventory item: {} by {}. New quantity: {}", 
                   savedItem.getName(), amount, savedItem.getQuantity());
        return ResponseEntity.ok(savedItem);
    }
}
