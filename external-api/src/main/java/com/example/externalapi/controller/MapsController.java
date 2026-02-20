package com.example.externalapi.controller;

import com.example.externalapi.service.FirebaseAuthService;
import com.example.externalapi.service.MapsService;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/maps")
public class MapsController {
    
    private static final Logger logger = LoggerFactory.getLogger(MapsController.class);
    
    @Autowired
    private MapsService mapsService;
    
    @Autowired
    private FirebaseAuthService firebaseAuthService;
    
    /**
     * Get directions from origin to destination
     */
    @GetMapping("/directions")
    public ResponseEntity<?> getDirections(
            @RequestParam String origin,
            @RequestParam String destination) {
        
        try {
            logger.info("Received directions request from: {} to: {}", origin, destination);
            
            // Call the service to get directions
            Object response = mapsService.getDirections(origin, destination);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing directions request", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch directions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get Google Maps API key for frontend usage
     */
    @GetMapping("/key")
    public ResponseEntity<?> getMapsApiKey(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        try {
            // For the API key, we'll keep it simple and not require authentication
            // In a production environment, you might want to authenticate or use rate limiting
            Map<String, String> response = new HashMap<>();
            response.put("key", mapsService.getApiKey());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error providing API key", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get API key: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Admin endpoint example - could be used for analytics or admin-only features
     */
    @GetMapping("/admin/usage")
    public ResponseEntity<?> getMapUsageStats(@RequestHeader("Authorization") String bearerToken) {
        try {
            // Verify the token
            FirebaseToken token = firebaseAuthService.verifyToken(bearerToken);
            if (token == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid authentication token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Check if user is admin
            boolean isAdmin = firebaseAuthService.checkAdminRole(token);
            if (!isAdmin) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Admin access required");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            // Placeholder for actual usage statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("dailyRequests", 250);
            stats.put("monthlyRequests", 7500);
            stats.put("costEstimate", "$15.75");
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error getting map usage stats", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get usage stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}