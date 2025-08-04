package com.rajeswarandhandapani.dblocking.service;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import com.rajeswarandhandapani.dblocking.repository.InventoryItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    public InventoryItem createItem(InventoryItem item) {
        logger.info("Creating new inventory item: {}", item.getName());
        return inventoryItemRepository.save(item);
    }

    public Optional<InventoryItem> getItemById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    /**
     * Update inventory quantity using optimistic locking.
     * This method demonstrates how optimistic locking handles concurrent updates.
     */
    @Transactional
    public InventoryItem updateQuantity(Long id, int newQuantity) throws InterruptedException {
        logger.info("Attempting to update quantity for inventory item ID: {} to {}", id, newQuantity);
        
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(id);
        
        if (optionalItem.isEmpty()) {
            throw new IllegalArgumentException("Inventory item not found with ID: " + id);
        }
        
        InventoryItem item = optionalItem.get();
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        // Simulate some processing time to increase chance of concurrent access
        Thread.sleep(5000);
        
        item.setQuantity(newQuantity);
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        logger.info("Successfully updated inventory item: {} to quantity {}", savedItem.getName(), savedItem.getQuantity());
        return savedItem;
    }

    /**
     * Reduce inventory quantity (simulating a purchase)
     */
    @Transactional
    public InventoryItem reduceQuantity(Long id, int amount) throws InterruptedException {
        logger.info("Attempting to reduce quantity for inventory item ID: {} by {}", id, amount);
        
        Optional<InventoryItem> optionalItem = inventoryItemRepository.findById(id);
        
        if (optionalItem.isEmpty()) {
            throw new IllegalArgumentException("Inventory item not found with ID: " + id);
        }
        
        InventoryItem item = optionalItem.get();
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Reduction amount must be positive");
        }
        
        if (item.getQuantity() < amount) {
            throw new IllegalStateException("Insufficient quantity available. Current: " + item.getQuantity() + ", Requested: " + amount);
        }
        
        // Simulate some processing time
        Thread.sleep(1000);
        
        item.setQuantity(item.getQuantity() - amount);
        InventoryItem savedItem = inventoryItemRepository.save(item);
        
        logger.info("Successfully reduced inventory item: {} by {}. New quantity: {}", 
                   savedItem.getName(), amount, savedItem.getQuantity());
        return savedItem;
    }
}
