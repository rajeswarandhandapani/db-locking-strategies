package com.rajeswarandhandapani.dblocking.controller;

import com.rajeswarandhandapani.dblocking.model.InventoryItem;
import com.rajeswarandhandapani.dblocking.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<InventoryItem> getAllItems() {
        return inventoryService.getAllItems();
    }

    @PostMapping
    public InventoryItem createItem(@RequestBody InventoryItem item) {
        return inventoryService.createItem(item);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItem> getItem(@PathVariable Long id) {
        Optional<InventoryItem> item = inventoryService.getItemById(id);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update inventory quantity using optimistic locking.
     * This endpoint demonstrates how optimistic locking handles concurrent updates.
     */
    @PutMapping("/{id}/update-quantity")
    public ResponseEntity<InventoryItem> updateQuantity(@PathVariable Long id, @RequestParam int newQuantity) throws InterruptedException {
        InventoryItem updatedItem = inventoryService.updateQuantity(id, newQuantity);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * Reduce inventory quantity (simulating a purchase)
     */
    @PostMapping("/{id}/reduce")
    public ResponseEntity<InventoryItem> reduceQuantity(@PathVariable Long id, @RequestParam int amount) throws InterruptedException {
        InventoryItem updatedItem = inventoryService.reduceQuantity(id, amount);
        return ResponseEntity.ok(updatedItem);
    }
}
