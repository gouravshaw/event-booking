package com.example.booking.service;

import com.example.booking.model.Booking;
import com.example.booking.model.Event;
import com.example.booking.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private EventService eventService;
    
    // Create a new booking
    public Booking createBooking(String userFirebaseUid, int eventId, int tickets, String authToken) {
        try {
            logger.info("Creating booking for user: {}, event: {}, tickets: {}", userFirebaseUid, eventId, tickets);
            
            // Step 1: Check if event exists and has enough tickets
            Event event = eventService.getEventById(eventId, authToken);
            
            if (event == null) {
                logger.error("Event not found: {}", eventId);
                throw new RuntimeException("Event not found");
            }
            
            logger.info("Event found: {}, available tickets: {}", event.getName(), event.getAvailableTickets());
            
            if (event.getAvailableTickets() < tickets) {
                logger.error("Not enough tickets available. Requested: {}, Available: {}", tickets, event.getAvailableTickets());
                throw new RuntimeException("Not enough tickets available");
            }
            
            // Step 2: Calculate total price
            double totalPrice = event.getPrice() * tickets;
            logger.info("Total price calculated: {}", totalPrice);
            
            // Step 3: Book tickets at Event Service
            boolean bookingSuccessful = eventService.bookTickets(eventId, tickets, authToken);
            
            if (!bookingSuccessful) {
                logger.error("Failed to book tickets at Event Service for event: {}", eventId);
                throw new RuntimeException("Failed to book tickets at Event Service");
            }
            
            logger.info("Successfully booked tickets at Event Service");
            
            // Step 4: Create and save the booking
            Booking booking = new Booking(userFirebaseUid, eventId, tickets, totalPrice);
            Booking savedBooking = bookingRepository.save(booking);
            
            logger.info("Booking created successfully with ID: {}", savedBooking.getId());
            
            return savedBooking;
        } catch (Exception e) {
            logger.error("Error creating booking for event {} and user {}: {}", eventId, userFirebaseUid, e.getMessage(), e);
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
    }
    
    // Cancel a booking
    public Booking cancelBooking(String bookingId, String userFirebaseUid, String authToken) {
        logger.info("Attempting to cancel booking: {} for user: {}", bookingId, userFirebaseUid);
        
        // Find the booking
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Check if this booking belongs to the user (skip this check for admin users)
            if (!booking.getUserFirebaseUid().equals(userFirebaseUid)) {
                logger.error("User {} attempted to cancel booking {} which belongs to user {}", 
                    userFirebaseUid, bookingId, booking.getUserFirebaseUid());
                throw new RuntimeException("You are not authorized to cancel this booking");
            }
            
            // Check if the booking is already cancelled
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                logger.error("Booking {} is already cancelled", bookingId);
                throw new RuntimeException("This booking is already cancelled");
            }
            
            // Return tickets to the event
            boolean returnSuccessful = eventService.returnTickets(
                    booking.getEventId(), booking.getTicketsBooked(), authToken);
            
            if (!returnSuccessful) {
                logger.error("Failed to return tickets to Event Service for booking: {}", bookingId);
                throw new RuntimeException("Failed to return tickets to Event Service");
            }
            
            logger.info("Successfully returned tickets to Event Service");
            
            // Update booking status
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            Booking updatedBooking = bookingRepository.save(booking);
            
            logger.info("Booking {} successfully cancelled", bookingId);
            
            return updatedBooking;
        } else {
            logger.error("Booking not found: {}", bookingId);
            throw new RuntimeException("Booking not found");
        }
    }
    
    // Get bookings for a specific user
    public List<Booking> getUserBookings(String userFirebaseUid) {
        logger.info("Getting bookings for user: {}", userFirebaseUid);
        List<Booking> bookings = bookingRepository.findByUserFirebaseUid(userFirebaseUid);
        logger.info("Found {} bookings for user", bookings.size());
        return bookings;
    }
    
    // Get bookings for a specific event
    public List<Booking> getEventBookings(int eventId) {
        logger.info("Getting bookings for event: {}", eventId);
        List<Booking> bookings = bookingRepository.findByEventId(eventId);
        logger.info("Found {} bookings for event", bookings.size());
        return bookings;
    }
    
    // Get a specific booking by ID
    public Optional<Booking> getBookingById(String bookingId) {
        logger.info("Getting booking by ID: {}", bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        
        if (booking.isPresent()) {
            logger.info("Booking found: {}", bookingId);
        } else {
            logger.info("Booking not found: {}", bookingId);
        }
        
        return booking;
    }
    
    // Delete a booking
    public void deleteBooking(String bookingId) {
        logger.info("Attempting to delete booking: {}", bookingId);
        
        if (bookingRepository.existsById(bookingId)) {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            
            if (bookingOpt.isPresent() && bookingOpt.get().getStatus() != Booking.BookingStatus.CANCELLED) {
                // If the booking is not already cancelled, return the tickets
                // We need to handle this case so that tickets are properly returned to available inventory
                try {
                    logger.info("Returning tickets for booking: {} before deletion", bookingId);
                    // We're sending a null authToken here, which might not work in your system.
                    // You might need to handle this differently, perhaps with an admin token or a system token.
                    eventService.returnTickets(
                            bookingOpt.get().getEventId(), 
                            bookingOpt.get().getTicketsBooked(), 
                            null);
                    
                    logger.info("Successfully returned tickets to Event Service");
                } catch (Exception e) {
                    logger.error("Error returning tickets during booking deletion: {}", e.getMessage(), e);
                    // Continue with deletion even if returning tickets fails
                }
            }
            
            bookingRepository.deleteById(bookingId);
            logger.info("Booking deleted: {}", bookingId);
        } else {
            logger.error("Booking not found for deletion: {}", bookingId);
            throw new RuntimeException("Booking not found");
        }
    }
    
    // Get all bookings
    public List<Booking> getAllBookings() {
        logger.info("Getting all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        logger.info("Found {} total bookings", bookings.size());
        return bookings;
    }
}