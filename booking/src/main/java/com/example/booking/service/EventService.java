package com.example.booking.service;

import com.example.booking.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EventService {
    
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SqsPublisher sqsPublisher;
    
    @Value("${event.service.url}")
    private String eventServiceUrl;
    
    // Get event details from Event Service
    public Event getEventById(int eventId, String authToken) {
        try {
            // Build the URL to call the Event Service
            String url = eventServiceUrl + "/api/public/events/" + eventId;
            
            logger.info("Getting event from Event Service: {}", url);
            
            // Set up headers with auth token
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", authToken);
                logger.debug("Added auth token to request headers");
            }
            
            // Create HTTP entity with headers
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Make the request to Event Service
            ResponseEntity<Event> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Event.class);
            
            // Log the response status
            logger.info("Event Service response status: {}", response.getStatusCode());
            
            // Return the event if found
            Event event = response.getBody();
            if (event != null) {
                logger.info("Retrieved event: ID={}, Name={}, AvailableTickets={}", 
                    event.getId(), event.getName(), event.getAvailableTickets());
            } else {
                logger.warn("Event service returned null event for ID: {}", eventId);
            }
            
            return event;
        } catch (Exception e) {
            logger.error("Error getting event from Event Service: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Book tickets at Event Service (SQS first, HTTP fallback)
    public boolean bookTickets(int eventId, int tickets, String authToken) {
        // Try SQS first for async decoupled communication
        if (sqsPublisher.isEnabled()) {
            boolean sent = sqsPublisher.publishTicketUpdate(eventId, -tickets);
            if (sent) {
                logger.info("Ticket update published via SQS for event: {}", eventId);
                return true;
            }
            logger.warn("SQS publish failed, falling back to HTTP for event: {}", eventId);
        }

        // Fallback to direct HTTP call
        try {
            String url = eventServiceUrl + "/api/internal/events/" + eventId + "/tickets?ticketChange=-" + tickets;
            logger.info("Booking tickets at Event Service via HTTP: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", authToken);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Event> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Event.class);
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("Event Service booking response: status={}, success={}", response.getStatusCode(), success);
            return success;
        } catch (Exception e) {
            logger.error("Error booking tickets from Event Service: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // Return tickets to Event Service (SQS first, HTTP fallback)
    public boolean returnTickets(int eventId, int tickets, String authToken) {
        // Try SQS first
        if (sqsPublisher.isEnabled()) {
            boolean sent = sqsPublisher.publishTicketUpdate(eventId, tickets);
            if (sent) {
                logger.info("Ticket return published via SQS for event: {}", eventId);
                return true;
            }
            logger.warn("SQS publish failed, falling back to HTTP for event: {}", eventId);
        }

        // Fallback to direct HTTP call
        try {
            String url = eventServiceUrl + "/api/internal/events/" + eventId + "/tickets?ticketChange=" + tickets;
            logger.info("Returning tickets to Event Service via HTTP: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            if (authToken != null && !authToken.isEmpty()) {
                headers.set("Authorization", authToken);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Event> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Event.class);
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("Event Service return tickets response: status={}, success={}", response.getStatusCode(), success);
            return success;
        } catch (Exception e) {
            logger.error("Error returning tickets from Event Service: {}", e.getMessage(), e);
            return false;
        }
    }
}