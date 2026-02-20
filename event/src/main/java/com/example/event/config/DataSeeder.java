package com.example.event.config;

import com.example.event.model.Event;
import com.example.event.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    
    @Autowired
    private EventRepository eventRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Add retry logic for MongoDB connection
        int maxRetries = 5;
        int retryCount = 0;
        boolean seeded = false;
        
        while (!seeded && retryCount < maxRetries) {
            try {
                // Check if data already exists
                long count = eventRepository.count();
                if (count > 0) {
                    logger.info("Data already seeded, skipping...");
                    return;
                }
                
                logger.info("Seeding event data...");
                
                // Create a list to hold the events
                List<Event> events = new ArrayList<>();
                
                // Conference events (3)
                events.add(createEvent(1, "Tech Conference 2025", "Conference", 200, 99.99,
                        "ExCeL London", "Royal Victoria Dock, 1 Western Gateway", "London", "UK", "E16 1XL",
                        "2025-05-15", "09:00", 8, null));

                events.add(createEvent(2, "Business Leadership Summit", "Conference", 150, 149.99,
                        "Manchester Central Convention Complex", "Petersfield", "Manchester", "UK", "M2 3GX",
                        "2025-06-20", "10:00", 6, null));

                events.add(createEvent(3, "Medical Research Conference", "Conference", 180, 129.99,
                        "Edinburgh International Conference Centre", "The Exchange, 150 Morrison St", "Edinburgh", "UK", "EH3 8EE",
                        "2025-07-10", "08:30", 9, null));

                // Concert events (3)
                events.add(createEvent(4, "Rock Festival 2025", "Concert", 5000, 89.99,
                        "Utilita Arena", "Arena Way, Gateshead", "Newcastle", "UK", "NE4 7NA",
                        "2025-07-25", "18:00", 5, null));

                events.add(createEvent(5, "Classical Orchestra Night", "Concert", 600, 59.99,
                        "Symphony Hall", "Broad Street", "Birmingham", "UK", "B1 2EA",
                        "2025-06-15", "19:30", 3, null));

                events.add(createEvent(6, "Jazz & Blues Evening", "Concert", 300, 45.00,
                        "Glasgow Royal Concert Hall", "2 Sauchiehall St", "Glasgow", "UK", "G2 3NY",
                        "2025-08-05", "20:00", 4, null));

                // Theater events (3)
                events.add(createEvent(7, "Shakespeare's Hamlet", "Theater", 250, 65.00,
                        "Royal Shakespeare Theatre", "Waterside", "Stratford-upon-Avon", "UK", "CV37 6BB",
                        "2025-09-10", "19:00", 3, null));

                events.add(createEvent(8, "Modern Dance Performance", "Theater", 200, 49.99,
                        "Bristol Hippodrome", "St Augustine's Parade", "Bristol", "UK", "BS1 4UZ",
                        "2025-08-22", "20:00", 2, null));

                events.add(createEvent(9, "Musical: The Lion King", "Theater", 350, 79.99,
                        "The Grand Theatre", "Church Street", "Blackpool", "UK", "FY1 1HT",
                        "2025-10-05", "18:30", 3, null));

                // Sports events (3)
                events.add(createEvent(10, "Premier League: City vs United", "Sports", 60000, 120.00,
                        "Anfield", "Anfield Road", "Liverpool", "UK", "L4 0TH",
                        "2025-08-15", "15:00", 2, null));

                events.add(createEvent(11, "Tennis Championship Finals", "Sports", 10000, 85.00,
                        "Eastbourne Tennis Center", "Devonshire Park, College Road", "Eastbourne", "UK", "BN21 4JJ",
                        "2025-07-05", "14:00", 4, null));

                events.add(createEvent(12, "Rugby Tournament", "Sports", 30000, 65.00,
                        "Principality Stadium", "Westgate St", "Cardiff", "UK", "CF10 1NS",
                        "2025-09-20", "16:00", 3, null));

                // Festival events (3)
                events.add(createEvent(13, "Food & Drink Festival", "Festival", 1500, 25.00,
                        "Millennium Square", "Calverley St", "Leeds", "UK", "LS1 1UR",
                        "2025-06-25", "11:00", 6, null));

                events.add(createEvent(14, "Film Festival", "Festival", 800, 35.00,
                        "York Picturehouse", "13-17 Coney St", "York", "UK", "YO1 9QL",
                        "2025-10-15", "10:00", 10, null));

                events.add(createEvent(15, "Cultural Festival", "Festival", 2000, 15.00,
                        "Brighton Dome", "Church Street", "Brighton", "UK", "BN1 1UE", 
                        "2025-08-30", "12:00", 8, null));
                
                // Save all events to the database
                logger.info("Saving {} events to database", events.size());
                eventRepository.saveAll(events);
                
                seeded = true; // Successfully seeded the database
            } catch (Exception e) {
                retryCount++;
                logger.warn("Failed to seed data (attempt {} of {}): {}", retryCount, maxRetries, e.getMessage());
                if (retryCount < maxRetries) {
                    // Wait for a bit before retrying
                    try {
                        logger.info("Waiting 10 seconds before retrying...");
                        Thread.sleep(10000); // 10 seconds
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logger.error("Failed to seed data after {} attempts", maxRetries, e);
                    throw e;
                }
            }
        }
    }
    
    private Event createEvent(int id, String name, String type, int availableTickets, double price,
                             String venue, String address, String city, String country, String postcode,
                             String date, String time, int duration, String imageData) {
        Event event = new Event();
        event.setId(id);
        event.setName(name);
        event.setType(type);
        event.setAvailableTickets(availableTickets);
        event.setPrice(price);
        event.setVenue(venue);
        event.setAddress(address);
        event.setCity(city);
        event.setCountry(country);
        event.setPostcode(postcode);
        event.setDate(date);
        event.setTime(time);
        event.setDuration(duration);
        event.setImageData(imageData);
        return event;
    }
}