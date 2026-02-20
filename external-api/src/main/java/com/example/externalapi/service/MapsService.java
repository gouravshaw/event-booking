package com.example.externalapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MapsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MapsService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    private final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    
    public MapsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get directions from origin to destination
     */
    public Object getDirections(String origin, String destination) {
        try {
            // Build the URL with parameters
            String url = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_API_URL)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .queryParam("key", apiKey)
                    .toUriString();
            
            logger.info("Calling Google Directions API for route from {} to {}", origin, destination);
            
            // Make the API request
            Object result = restTemplate.getForObject(url, Object.class);
            
            logger.info("Successfully retrieved directions");
            return result;
        } catch (Exception e) {
            logger.error("Error getting directions from Google API", e);
            throw new RuntimeException("Failed to get directions: " + e.getMessage());
        }
    }
    
    /**
     * Get API key for embed maps
     */
    public String getApiKey() {
        return apiKey;
    }
}