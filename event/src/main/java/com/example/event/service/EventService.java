package com.example.event.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.event.model.Event;
import com.example.event.repository.EventRepository;

@Service
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private S3Service s3Service;

     @Autowired
    private RestTemplate restTemplate; 

    // Get all events
    public List<Event> getAllEvents() {
        try {
            return eventRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all events", e);
            throw e;
        }
    }

    // Add a new event
    public Event addEvent(Event event) {
        try {
            // Find maximum existing ID and add 1 for new event
            int newId = 1;
            List<Event> allEvents = eventRepository.findAll();
            
            if (!allEvents.isEmpty()) {
                int maxId = 0;
                for (Event existingEvent : allEvents) {
                    if (existingEvent.getId() > maxId) {
                        maxId = existingEvent.getId();
                    }
                }
                newId = maxId + 1;
            }
            
            event.setId(newId);

            // Upload image to S3 if configured; otherwise keep base64 in MongoDB
            if (s3Service.isEnabled() && event.getImageData() != null && !event.getImageData().isEmpty()) {
                String s3Url = s3Service.uploadImage(event.getImageData());
                if (s3Url != null) {
                    event.setImageData(s3Url);
                }
            }

            return eventRepository.save(event);
        } catch (Exception e) {
            logger.error("Error adding event", e);
            throw e;
        }
    }

    // Get event by ID
    public Event getEventById(int id) {
        try {
            Optional<Event> event = eventRepository.findById(id);
            if (event.isPresent()) {
                return event.get();
            }
            return null;
        } catch (Exception e) {
            logger.error("Error getting event by ID: {}", id, e);
            throw e;
        }
    }

    // Update existing event
    public Event updateEvent(int id, Event updated) {
        try {
            Event existing = getEventById(id);
            if (existing == null) {
                return null;
            }

            // Handle S3 image: upload new image if it's base64, delete old one
            if (s3Service.isEnabled() && updated.getImageData() != null && !updated.getImageData().isEmpty()) {
                boolean isBase64 = updated.getImageData().startsWith("data:") || !updated.getImageData().startsWith("http");
                if (isBase64) {
                    // Delete old S3 image if it exists
                    if (existing.getImageData() != null && existing.getImageData().contains("amazonaws.com")) {
                        s3Service.deleteImage(existing.getImageData());
                    }
                    String s3Url = s3Service.uploadImage(updated.getImageData());
                    if (s3Url != null) {
                        updated.setImageData(s3Url);
                    }
                }
            }

            updated.setId(id);
            return eventRepository.save(updated);
        } catch (Exception e) {
            logger.error("Error updating event with ID: {}", id, e);
            throw e;
        }
    }

    public boolean hasActiveBookings(int eventId) {
    try {
        // Call the Booking Service to check for active bookings
        String url = "http://booking-service:8080/booking/api/internal/events/" + eventId + "/bookings";
        
        // Make the request to Booking Service
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // If we get a successful response, check if there are any bookings
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // Check if the response indicates any bookings
            return !response.getBody().equals("[]"); // If not empty, there are bookings
        }
        
        return false; // Assume no bookings if we can't confirm
    } catch (Exception e) {
        logger.error("Error checking for active bookings for event ID: {}", eventId, e);
        return false; // Assume no bookings if service is down
    }
}

    // Delete event
public boolean deleteEvent(int id) {
    try {
        Event event = getEventById(id);
        if (event == null) {
            return false;
        }

        // First check if there are active bookings
        boolean hasBookings = hasActiveBookings(id);
        if (hasBookings) {
            logger.warn("Cannot delete event ID: {} - event has active bookings", id);
            throw new IllegalStateException("Cannot delete event with active bookings");
        }

        // Delete S3 image if applicable
        if (s3Service.isEnabled() && event.getImageData() != null
                && event.getImageData().contains("amazonaws.com")) {
            s3Service.deleteImage(event.getImageData());
        }

        eventRepository.deleteById(id);
        return true;
    } catch (IllegalStateException e) {
        logger.error("Cannot delete event: {}", e.getMessage());
        throw e; // Rethrow to be handled by controller
    } catch (Exception e) {
        logger.error("Error deleting event with ID: {}", id, e);
        throw e;
    }
}

    // Book tickets for an event
    public Event bookTickets(int eventId, int tickets) {
        try {
            // Input validation
            if (tickets <= 0) {
                throw new IllegalArgumentException("Number of tickets must be positive");
            }
            
            // Get event
            Event event = getEventById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found");
            }
            
            // Check ticket availability
            if (event.getAvailableTickets() < tickets) {
                throw new IllegalArgumentException("Not enough tickets available");
            }
            
            // Update available tickets
            event.setAvailableTickets(event.getAvailableTickets() - tickets);
            
            // Save updated event
            return eventRepository.save(event);
        } catch (Exception e) {
            logger.error("Error booking tickets", e);
            throw e;
        }
    }

    // Return tickets to an event
    public Event returnTickets(int eventId, int tickets) {
        try {
            // Input validation
            if (tickets <= 0) {
                throw new IllegalArgumentException("Number of tickets must be positive");
            }
            
            // Get event
            Event event = getEventById(eventId);
            if (event == null) {
                throw new RuntimeException("Event not found");
            }
            
            // Update available tickets
            event.setAvailableTickets(event.getAvailableTickets() + tickets);
            
            // Save updated event
            return eventRepository.save(event);
        } catch (Exception e) {
            logger.error("Error returning tickets", e);
            throw e;
        }
    }
}