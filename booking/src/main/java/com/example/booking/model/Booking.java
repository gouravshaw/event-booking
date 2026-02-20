package com.example.booking.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;
    private String userFirebaseUid;
    private int eventId;
    private int ticketsBooked;
    private double totalPrice;
    private String currency;
    private LocalDateTime bookingTime;
    private BookingStatus status;

    // Possible booking statuses
    public enum BookingStatus {
        CONFIRMED,  // Booking is confirmed
        CANCELLED,  // Booking was cancelled by the user
        PENDING     // Booking is waiting for confirmation
    }

    // Default constructor
    public Booking() {
        this.bookingTime = LocalDateTime.now();
        this.status = BookingStatus.CONFIRMED;
        this.currency = "GBP";
    }

    // Constructor with basic booking details
    public Booking(String userFirebaseUid, int eventId, int ticketsBooked, double totalPrice) {
        this();
        this.userFirebaseUid = userFirebaseUid;
        this.eventId = eventId;
        this.ticketsBooked = ticketsBooked;
        this.totalPrice = totalPrice;
    }

    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserFirebaseUid() {
        return userFirebaseUid;
    }
    
    public void setUserFirebaseUid(String userFirebaseUid) {
        this.userFirebaseUid = userFirebaseUid;
    }
    
    public int getEventId() {
        return eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    
    public int getTicketsBooked() {
        return ticketsBooked;
    }
    
    public void setTicketsBooked(int ticketsBooked) {
        this.ticketsBooked = ticketsBooked;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getBookingTime() {
        return bookingTime;
    }
    
    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    // Convert Booking to JSONObject for API responses
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        
        // Add booking details to JSON
        json.put("id", id);
        json.put("userFirebaseUid", userFirebaseUid);
        json.put("eventId", eventId);
        json.put("ticketsBooked", ticketsBooked);
        json.put("totalPrice", totalPrice);
        json.put("currency", currency);
        
        // Format date and time
        if (bookingTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            json.put("bookingTime", bookingTime.format(formatter));
        } else {
            json.put("bookingTime", JSONObject.NULL);
        }
        
        json.put("status", status.toString());
        
        return json;
    }
}