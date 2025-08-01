package com.rajeswarandhandapani.dblocking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Spring Boot Database Locking Strategy Showcase");
        response.put("description", "This application demonstrates pessimistic and optimistic locking strategies");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /api/tickets", "List all tickets");
        endpoints.put("POST /api/tickets", "Create a new ticket");
        endpoints.put("GET /api/tickets/{id}", "Get ticket by ID");
        endpoints.put("POST /api/tickets/{id}/book", "Book a ticket (Pessimistic Locking Demo)");
        endpoints.put("POST /api/tickets/{id}/cancel", "Cancel ticket booking");
        endpoints.put("GET /api/inventory", "List all inventory items");
        endpoints.put("POST /api/inventory", "Create a new inventory item");
        endpoints.put("GET /api/inventory/{id}", "Get inventory item by ID");
        endpoints.put("PUT /api/inventory/{id}/update-quantity?newQuantity=X", "Update quantity (Optimistic Locking Demo)");
        endpoints.put("POST /api/inventory/{id}/reduce?amount=X", "Reduce quantity (Optimistic Locking Demo)");
        
        response.put("endpoints", endpoints);
        
        Map<String, String> lockingInfo = new HashMap<>();
        lockingInfo.put("Pessimistic Locking", "Used in ticket booking - prevents concurrent access by acquiring locks");
        lockingInfo.put("Optimistic Locking", "Used in inventory management - detects conflicts using version numbers");
        
        response.put("lockingStrategies", lockingInfo);
        
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Database Locking Strategies Demo is running");
        return response;
    }
}
