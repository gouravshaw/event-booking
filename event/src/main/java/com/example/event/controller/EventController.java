package com.example.event.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.event.model.Event;
import com.example.event.service.EventService;
import com.example.event.service.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;

@RestController
public class EventController {
    
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private FirebaseAuthService firebaseAuthService;
    
    // PUBLIC APIs (No Authentication Required)
    
    // Get all events
    @GetMapping("/api/public/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }
    
    // Get a specific event by ID
    @GetMapping("/api/public/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable int id) {
        Event event = eventService.getEventById(id);
        if (event != null) {
            return ResponseEntity.ok(event);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Search for events by type, city, and date
    @GetMapping("/api/public/events/search")
    public ResponseEntity<List<Event>> searchEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date) {
        
        // Get all events
        List<Event> allEvents = eventService.getAllEvents();
        
        // Filter events manually based on search criteria
        List<Event> filteredEvents = new ArrayList<>();
        
        for (Event event : allEvents) {
            boolean matchesType = (type == null) || event.getType().equalsIgnoreCase(type);
            boolean matchesCity = (city == null) || event.getCity().equalsIgnoreCase(city);
            boolean matchesDate = (date == null) || event.getDate().equals(date);
            
            if (matchesType && matchesCity && matchesDate) {
                filteredEvents.add(event);
            }
        }
        
        return ResponseEntity.ok(filteredEvents);
    }
    
    // ADMIN APIs (Admin Authentication Required)
    
    // Add a new event
    @PostMapping("/api/admin/events")
    public ResponseEntity<?> addEvent(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody Event event) {
        
        try {
            // Verify token
            FirebaseToken token = firebaseAuthService.verifyToken(bearerToken);
            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid authentication token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Create the event
            Event createdEvent = eventService.addEvent(event);
            return ResponseEntity.ok(createdEvent);
        } catch (Exception e) {
            logger.error("Error adding event", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Update an existing event
    @PutMapping("/api/admin/events/{id}")
    public ResponseEntity<?> updateEvent(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable int id,
            @RequestBody Event event) {
        
        try {
            // Verify token
            FirebaseToken token = firebaseAuthService.verifyToken(bearerToken);
            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid authentication token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Update the event
            Event updatedEvent = eventService.updateEvent(id, event);
            if (updatedEvent != null) {
                return ResponseEntity.ok(updatedEvent);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating event", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update event: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Delete an event
   @DeleteMapping("/api/admin/events/{id}")
public ResponseEntity<?> deleteEvent(
        @RequestHeader("Authorization") String bearerToken,
        @PathVariable int id) {
    
    try {
        // Verify token
        FirebaseToken token = firebaseAuthService.verifyToken(bearerToken);
        if (token == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid authentication token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        
        // Delete the event
        boolean deleted = eventService.deleteEvent(id);
        if (deleted) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Event deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    } catch (IllegalStateException e) {
        // Special handling for events with active bookings
        logger.error("Cannot delete event: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    } catch (Exception e) {
        logger.error("Error deleting event", e);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Failed to delete event: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
    
    // INTERNAL APIs (Service-to-Service Communication)
    
    // Update ticket availability
    @PutMapping("/api/internal/events/{id}/tickets")
    public ResponseEntity<?> updateTicketAvailability(
            @PathVariable int id,
            @RequestParam int ticketChange) {
        
        try {
            Event event = eventService.getEventById(id);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Update available tickets
            int newAvailability = event.getAvailableTickets() + ticketChange;
            if (newAvailability < 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not enough tickets available");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Update the event
            event.setAvailableTickets(newAvailability);
            Event updatedEvent = eventService.updateEvent(id, event);
            
            return ResponseEntity.ok(updatedEvent);
        } catch (Exception e) {
            logger.error("Error updating ticket availability", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update ticket availability: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}