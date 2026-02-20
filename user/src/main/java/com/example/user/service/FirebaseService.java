package com.example.user.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    
    public FirebaseToken verifyToken(String idToken) {
        try {
            // Clean token (remove Bearer if present)
            if (idToken.startsWith("Bearer ")) {
                idToken = idToken.substring(7);
            }
            
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying Firebase token", e);
            return null;
        }
    }
    
    public UserRecord getUserByEmail(String email) {
        try {
            return FirebaseAuth.getInstance().getUserByEmail(email);
        } catch (FirebaseAuthException e) {
            logger.error("Error getting user by email: {}", email, e);
            return null;
        }
    }
    
    public UserRecord getUserByUid(String uid) {
        try {
            return FirebaseAuth.getInstance().getUser(uid);
        } catch (FirebaseAuthException e) {
            logger.error("Error getting user by uid: {}", uid, e);
            return null;
        }
    }
    
    public UserRecord createUser(String email, String password) {
        try {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setEmailVerified(false)
                    .setPassword(password)
                    .setDisabled(false);
            
            return FirebaseAuth.getInstance().createUser(request);
        } catch (FirebaseAuthException e) {
            logger.error("Error creating user with email: {}", email, e);
            throw new RuntimeException("Failed to create Firebase user: " + e.getMessage());
        }
    }
}