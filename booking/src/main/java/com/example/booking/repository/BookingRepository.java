package com.example.booking.repository;

import com.example.booking.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    
    List<Booking> findByUserFirebaseUid(String userFirebaseUid);
    
    List<Booking> findByEventId(int eventId);
    
    List<Booking> findByUserFirebaseUidAndEventId(String userFirebaseUid, int eventId);
}