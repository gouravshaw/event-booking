package com.example.event.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
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
    
    public boolean checkAdminRole(FirebaseToken token) {
        try {
            // Get the user's Firebase UID from the token
            String firebaseUid = token.getUid();
            
            // Query MongoDB directly to check if this user has admin role
            Query query = new Query();
            query.addCriteria(Criteria.where("firebaseUid").is(firebaseUid));
            
            Document userDoc = mongoTemplate.findOne(query, Document.class, "users");
            
            if (userDoc != null) {
                String role = userDoc.getString("role");
                return "ADMIN".equals(role);
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error checking admin role", e);
            return false;
        }
    }
}