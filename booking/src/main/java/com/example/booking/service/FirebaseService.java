package com.example.booking.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);
    
    public FirebaseToken verifyToken(String idToken) {
        try {
            // Clean token (remove Bearer if present)
            if (idToken != null && idToken.startsWith("Bearer ")) {
                idToken = idToken.substring(7);
                logger.debug("Removed 'Bearer ' prefix from token");
            }
            
            if (idToken == null || idToken.isEmpty()) {
                logger.error("Empty token provided for verification");
                return null;
            }
            
            logger.info("Verifying Firebase token (first 10 chars): {}...", idToken.substring(0, Math.min(10, idToken.length())));
            
            // Verify the token with Firebase
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            
            if (token != null) {
                logger.info("Token verified successfully for user: {}", token.getUid());
            } else {
                logger.warn("Firebase returned null token");
            }
            
            return token;
        } catch (FirebaseAuthException e) {
            logger.error("Error verifying Firebase token: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error verifying token: {}", e.getMessage(), e);
            return null;
        }
    }
}