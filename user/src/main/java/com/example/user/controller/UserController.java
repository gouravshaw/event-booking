package com.example.user.controller;

import com.example.user.model.User;
import com.example.user.service.FirebaseService;
import com.example.user.service.UserService;
import com.google.firebase.auth.FirebaseToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FirebaseService firebaseService;
    
    // PUBLIC APIs (No Authentication Required)
    
    // Register a new user
    @PostMapping("/api/public/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> registrationData) {
        try {
            // Extract registration data
            String email = registrationData.get("email");
            String password = registrationData.get("password");
            String fullName = registrationData.get("fullName");
            String phoneNumber = registrationData.get("phoneNumber");
            String adminSecret = registrationData.get("adminSecret");
            
            // Register the user
            User user = userService.registerUser(email, password, fullName, phoneNumber, adminSecret);
            
            // Create success response
            JSONObject response = new JSONObject();
            response.put("user", user.toJSON());
            response.put("message", "User registered successfully");
            
            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            logger.error("Registration error", e);
            
            // Create error response
            try {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse.toString());
            } catch (JSONException je) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }
    
    // USER APIs (Authentication Required)
    
    // Get current user's profile
    @GetMapping("/api/user/profile")
    public ResponseEntity<String> getUserProfile(@RequestHeader("Authorization") String bearerToken) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Get the user
                User user = userService.getUserByFirebaseUid(token.getUid());
                
                if (user != null) {
                    // Return user profile
                    return ResponseEntity.ok(user.toJSON().toString());
                } else {
                    // User not found
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"User not found\"}");
                }
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error getting user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get user profile\"}");
        }
    }
    
    // Update current user's profile
    @PutMapping("/api/user/profile")
    public ResponseEntity<String> updateProfile(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody User updatedUser) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Update the user
                User user = userService.updateUser(token.getUid(), updatedUser);
                return ResponseEntity.ok(user.toJSON().toString());
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update profile: " + e.getMessage() + "\"}");
        }
    }
    
    // ADMIN APIs (Admin Authentication Required)
    
    // Get all users
    @GetMapping("/api/admin/users")
    public ResponseEntity<String> getAllUsers(@RequestHeader("Authorization") String bearerToken) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Check admin role
                if (!userService.checkAdminRole(token)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Admin access required\"}");
                }
                
                // Get all users
                List<User> users = userService.getAllUsers();
                
                // Create response
                JSONObject response = new JSONObject();
                JSONArray usersArray = new JSONArray();
                
                // Add each user to the array
                for (User user : users) {
                    usersArray.put(user.toJSON());
                }
                
                response.put("users", usersArray);
                return ResponseEntity.ok(response.toString());
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get users: " + e.getMessage() + "\"}");
        }
    }
    
    // Get user by ID
    @GetMapping("/api/admin/users/{id}")
    public ResponseEntity<String> getUserById(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable String id) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Check admin role
                if (!userService.checkAdminRole(token)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Admin access required\"}");
                }
                
                // Get user
                Optional<User> user = userService.getUserById(id);
                
                if (user.isPresent()) {
                    return ResponseEntity.ok(user.get().toJSON().toString());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"error\": \"User not found\"}");
                }
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error getting user by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to get user: " + e.getMessage() + "\"}");
        }
    }
    
    // Update user by ID
    @PutMapping("/api/admin/users/{id}")
    public ResponseEntity<String> updateUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable String id,
            @RequestBody User updatedUser) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Check admin role
                if (!userService.checkAdminRole(token)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Admin access required\"}");
                }
                
                // Update user
                User user = userService.updateUserById(id, updatedUser);
                return ResponseEntity.ok(user.toJSON().toString());
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to update user: " + e.getMessage() + "\"}");
        }
    }
    
    // Delete user by ID
    @DeleteMapping("/api/admin/users/{id}")
    public ResponseEntity<String> deleteUser(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable String id) {
        try {
            // Verify the token
            FirebaseToken token = firebaseService.verifyToken(bearerToken);
            
            if (token != null) {
                // Check admin role
                if (!userService.checkAdminRole(token)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("{\"error\": \"Admin access required\"}");
                }
                
                // Delete user
                userService.deleteUser(id);
                
                // Create success response
                JSONObject response = new JSONObject();
                response.put("message", "User deleted successfully");
                
                return ResponseEntity.ok(response.toString());
            } else {
                // Invalid token
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid token\"}");
            }
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to delete user: " + e.getMessage() + "\"}");
        }
    }
}