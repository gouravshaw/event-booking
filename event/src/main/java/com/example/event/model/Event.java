package com.example.event.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "events")
public class Event {

    @Id
    private int id;
    private String name;
    private String type;
    private int availableTickets;
    private double price;
    private String venue;
    private String address;
    private String city;
    private String country;
    private String postcode;
    private String date;
    private String time;
    private int duration;
    private String imageData; // New field for storing base64 encoded image

    // Default constructor
    public Event() {
    }

    // Constructor with all fields
    public Event(int id, String name, String type, int availableTickets, double price,
                 String venue, String address, String city, String country, String postcode,
                 String date, String time, int duration, String imageData) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.availableTickets = availableTickets;
        this.price = price;
        this.venue = venue;
        this.address = address;
        this.city = city;
        this.country = country;
        this.postcode = postcode;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.imageData = imageData;
    }

    // Getters and setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }
    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getVenue() {
        return venue;
    }
    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostcode() {
        return postcode;
    }
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public String getImageData() {
        return imageData;
    }
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
    
    // Convert Event to JSONObject
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("type", type);
            json.put("availableTickets", availableTickets);
            json.put("price", price);
            json.put("venue", venue);
            json.put("address", address);
            json.put("city", city);
            json.put("country", country);
            json.put("postcode", postcode);
            json.put("date", date);
            json.put("time", time);
            json.put("duration", duration);
            json.put("imageData", imageData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    
    // Create Event from JSONObject
    public static Event fromJSON(JSONObject json) throws JSONException {
        Event event = new Event();
        
        // Extract ID if present (might be missing for new events)
        if (json.has("id") && !json.isNull("id")) {
            event.setId(json.getInt("id"));
        }
        
        // Extract required fields
        event.setName(json.getString("name"));
        event.setType(json.getString("type"));
        event.setAvailableTickets(json.getInt("availableTickets"));
        event.setPrice(json.getDouble("price"));
        event.setVenue(json.getString("venue"));
        event.setAddress(json.getString("address"));
        event.setCity(json.getString("city"));
        event.setCountry(json.getString("country"));
        
        // Extract postcode if present
        if (json.has("postcode") && !json.isNull("postcode")) {
            event.setPostcode(json.getString("postcode"));
        }
        
        event.setDate(json.getString("date"));
        event.setTime(json.getString("time"));
        event.setDuration(json.getInt("duration"));
        
        // Extract image data if present
        if (json.has("imageData") && !json.isNull("imageData")) {
            event.setImageData(json.getString("imageData"));
        }
        
        return event;
    }
}