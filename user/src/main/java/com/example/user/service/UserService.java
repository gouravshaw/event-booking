package com.example.user.service;

import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FirebaseService firebaseService;
    
    @Value("${admin.secret}")
    private String adminSecret;
    
    // Find user by email
    public User getUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return null;
    }
    
    // Find user by Firebase UID
    public User getUserByFirebaseUid(String firebaseUid) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return null;
    }
    
    // Find user by database ID
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }
    
    // Register new user
    public User registerUser(String email, String password, String fullName, String phoneNumber, String adminSecretProvided) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists");
        }
        
        try {
            // Create user in Firebase
            UserRecord firebaseUser = firebaseService.createUser(email, password);
            
            // Create user in our database
            User user = new User(firebaseUser.getUid(), email, fullName, phoneNumber);
            
            // Make user admin if correct secret provided
            if (adminSecretProvided != null && adminSecretProvided.equals(adminSecret)) {
                user.setRole(User.UserRole.ADMIN);
                logger.info("Created admin user: {}", email);
            } else {
                logger.info("Created regular user: {}", email);
            }
            
            // Save user to database
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Error registering user: {}", email, e);
            throw new RuntimeException("Failed to register user: " + e.getMessage());
        }
    }
    
    // Update user information
    public User updateUser(String firebaseUid, User updatedUser) {
        // Find the user
        User existingUser = getUserByFirebaseUid(firebaseUid);
        
        if (existingUser != null) {
            // Update fields if provided
            if (updatedUser.getFullName() != null) {
                existingUser.setFullName(updatedUser.getFullName());
            }
            
            if (updatedUser.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            }
            
            // Update timestamp
            existingUser.setUpdatedAt(LocalDateTime.now());
            
            // Save and return updated user
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    
    // Update user by ID (admin functionality)
    public User updateUserById(String id, User updatedUser) {
        // Find the user
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            
            // Update fields if provided
            if (updatedUser.getFullName() != null) {
                existingUser.setFullName(updatedUser.getFullName());
            }
            if (updatedUser.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            }
            if (updatedUser.getRole() != null) {
                existingUser.setRole(updatedUser.getRole());
            }
            
            // Update timestamp
            existingUser.setUpdatedAt(LocalDateTime.now());
            
            // Save and return
            return userRepository.save(existingUser);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    
    // Delete user
    public void deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    
    // Make user an admin
    public User makeUserAdmin(String id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(User.UserRole.ADMIN);
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Check if user has admin role
    public boolean checkAdminRole(FirebaseToken token) {
        User user = getUserByFirebaseUid(token.getUid());
        return user != null && user.getRole() == User.UserRole.ADMIN;
    }
}