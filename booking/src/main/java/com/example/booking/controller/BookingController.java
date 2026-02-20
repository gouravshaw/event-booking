package com.example.booking.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.booking.model.Booking;
import com.example.booking.service.BookingService;
import com.example.booking.service.FirebaseService;
import com.google.firebase.auth.FirebaseToken;

@RestController
public class BookingController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private FirebaseService firebaseService;
    
    // USER APIs (Authentication Required)
    
    // Create a new booking
    @PostMapping(value = "/api/user/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createBooking(
            @RequestHeader("Authorization") String authToken,
            @RequestBody Map<String, Object> bookingRequest) {
        
        try {
            // Log all incoming request information for debugging
            logger.info("Received booking request: {}", bookingRequest);
            logger.info("Auth token received (partial): {}...", authToken.substring(0, Math.min(20, authToken.length())));
            
            // Step 1: Verify user token
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("User authenticated successfully: {}", token.getUid());
            
            // Step 2: Extract booking details
            int eventId = Integer.parseInt(bookingRequest.get("eventId").toString());
            int tickets = Integer.parseInt(bookingRequest.get("tickets").toString());
            
            logger.info("Attempting to book: eventId={}, tickets={}", eventId, tickets);
            
            // Step 3: Create the booking
            Booking booking = bookingService.createBooking(
                    token.getUid(), eventId, tickets, authToken);
            
            logger.info("Booking created successfully: {}", booking.getId());
            
            // Step 4: Return success response
            return ResponseEntity.ok(booking.toJSON().toString());
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Cancel a booking
    @PutMapping(value = "/api/user/bookings/{id}/cancel", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> cancelBooking(
            @RequestHeader("Authorization") String authToken,
            @PathVariable String id) {
        
        try {
            logger.info("Received cancel booking request for booking: {}", id);
            
            // Step 1: Verify user token
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for cancel booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("User authenticated for cancel: {}", token.getUid());
            
            // Step 2: Cancel the booking
            Booking booking = bookingService.cancelBooking(id, token.getUid(), authToken);
            
            logger.info("Booking cancelled successfully: {}", id);
            
            // Step 3: Return success response
            return ResponseEntity.ok(booking.toJSON().toString());
        } catch (Exception e) {
            logger.error("Error cancelling booking: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Get all bookings for the current user
    @GetMapping(value = "/api/user/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserBookings(
            @RequestHeader("Authorization") String authToken) {
        
        try {
            logger.info("Received request for user bookings");
            
            // Step 1: Verify user token
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for getting user bookings");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("User authenticated for bookings list: {}", token.getUid());
            
            // Step 2: Get the user's bookings
            List<Booking> bookings = bookingService.getUserBookings(token.getUid());
            
            logger.info("Retrieved {} bookings for user", bookings.size());
            
            // Step 3: Convert to JSON array
            JSONArray bookingsArray = new JSONArray();
            for (Booking booking : bookings) {
                bookingsArray.put(booking.toJSON());
            }
            
            // Step 4: Return the bookings
            return ResponseEntity.ok(bookingsArray.toString());
        } catch (Exception e) {
            logger.error("Error getting user bookings: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get a specific booking for the current user
    @GetMapping(value = "/api/user/bookings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBookingById(
            @RequestHeader("Authorization") String authToken,
            @PathVariable String id) {
        
        try {
            logger.info("Received request for specific booking: {}", id);
            
            // Step 1: Verify user token
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for getting booking details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("User authenticated for booking details: {}", token.getUid());
            
            // Step 2: Get the booking
            Optional<Booking> booking = bookingService.getBookingById(id);
            
            if (booking.isPresent()) {
                // Step 3: Check if the booking belongs to this user
                if (!booking.get().getUserFirebaseUid().equals(token.getUid())) {
                    logger.error("User attempted to access unauthorized booking");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Not authorized to view this booking\"}");
                }
                
                logger.info("Booking details retrieved successfully: {}", id);
                
                // Step 4: Return the booking
                return ResponseEntity.ok(booking.get().toJSON().toString());
            } else {
                logger.error("Booking not found: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"Booking not found\"}");
            }
        } catch (Exception e) {
            logger.error("Error getting booking: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // ADMIN APIs (Admin Authentication Required)
    
    // Get all bookings in the system
    @GetMapping(value = "/api/admin/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllBookings(
            @RequestHeader("Authorization") String authToken) {
        
        try {
            logger.info("Received admin request for all bookings");
            
            // Step 1: Verify token (admin check should be added in real app)
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for admin bookings request");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("Admin authenticated for all bookings: {}", token.getUid());
            
            // Step 2: Get all bookings
            List<Booking> bookings = bookingService.getAllBookings();
            
            logger.info("Retrieved {} bookings total", bookings.size());
            
            // Step 3: Convert to JSON array
            JSONArray bookingsArray = new JSONArray();
            for (Booking booking : bookings) {
                bookingsArray.put(booking.toJSON());
            }
            
            // Step 4: Return the bookings
            return ResponseEntity.ok(bookingsArray.toString());
        } catch (Exception e) {
            logger.error("Error getting all bookings: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get any booking by ID (admin access)
    @GetMapping(value = "/api/admin/bookings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getBookingByIdAdmin(
            @RequestHeader("Authorization") String authToken,
            @PathVariable String id) {
        
        try {
            logger.info("Received admin request for booking: {}", id);
            
            // Step 1: Verify token (admin check should be added in real app)
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for admin booking details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("Admin authenticated for booking details: {}", token.getUid());
            
            // Step 2: Get the booking
            Optional<Booking> booking = bookingService.getBookingById(id);
            
            if (booking.isPresent()) {
                logger.info("Admin booking details retrieved successfully: {}", id);
                return ResponseEntity.ok(booking.get().toJSON().toString());
            } else {
                logger.error("Booking not found for admin: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"Booking not found\"}");
            }
        } catch (Exception e) {
            logger.error("Error getting booking by admin: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // checking for active booking before the event cancellation by admin
    @GetMapping(value = "/api/internal/events/{eventId}/bookings", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<String> getBookingsForEvent(@PathVariable int eventId) {
    try {
        logger.info("Checking for bookings for event: {}", eventId);
        
        // Get bookings for the event
        List<Booking> bookings = bookingService.getEventBookings(eventId);
        
        // Filter to only include active (non-cancelled) bookings
        List<Booking> activeBookings = bookings.stream()
            .filter(booking -> booking.getStatus() != Booking.BookingStatus.CANCELLED)
            .collect(Collectors.toList());
        
        // Convert to JSON array
        JSONArray bookingsArray = new JSONArray();
        for (Booking booking : activeBookings) {
            bookingsArray.put(booking.toJSON());
        }
        
        logger.info("Found {} active bookings for event {}", activeBookings.size(), eventId);
        
        // Return the bookings
        return ResponseEntity.ok(bookingsArray.toString());
    } catch (Exception e) {
        logger.error("Error getting bookings for event: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("[]"); // Return empty array on error
    }
}
    // Delete a booking (admin access)
    @DeleteMapping(value = "/api/admin/bookings/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteBooking(
            @RequestHeader("Authorization") String authToken,
            @PathVariable String id) {
        
        try {
            logger.info("Received admin request to delete booking: {}", id);
            
            // Step 1: Verify token (admin check should be added in real app)
            FirebaseToken token = firebaseService.verifyToken(authToken);
            
            if (token == null) {
                logger.error("Invalid authentication token for admin delete booking");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
            
            logger.info("Admin authenticated for delete booking: {}", token.getUid());
            
            // Step 2: Delete the booking
            bookingService.deleteBooking(id);
            
            logger.info("Booking deleted successfully: {}", id);
            
            // Step 3: Return success response
            JSONObject response = new JSONObject();
            response.put("message", "Booking deleted successfully");
            
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            logger.error("Error deleting booking: {}", e.getMessage(), e);
            return createErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Helper method for creating error responses
    private ResponseEntity<String> createErrorResponse(Exception e, HttpStatus status) {
        try {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return ResponseEntity.status(status).body(error.toString());
        } catch (JSONException je) {
            return ResponseEntity.status(status)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}