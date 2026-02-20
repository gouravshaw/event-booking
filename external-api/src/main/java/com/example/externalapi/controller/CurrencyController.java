package com.example.externalapi.controller;

import com.example.externalapi.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);
    
    @Autowired
    private CurrencyService currencyService;
    
    /**
     * Get all available currencies
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, String>> getCurrencies() {
        logger.info("Getting available currencies");
        Map<String, String> currencies = currencyService.getAvailableCurrencies();
        return ResponseEntity.ok(currencies);
    }
    
    /**
     * Get exchange rates
     */
    @GetMapping("/rates")
    public ResponseEntity<Map<String, Object>> getRates() {
        logger.info("Getting exchange rates");
        Map<String, Object> rates = currencyService.getExchangeRates();
        return ResponseEntity.ok(rates);
    }
    
    /**
     * Convert currency
     */
    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convertCurrency(
            @RequestParam double amount,
            @RequestParam String targetCurrency) {
        
        logger.info("Converting {} GBP to {}", amount, targetCurrency);
        
        try {
            double convertedAmount = currencyService.convertCurrency(amount, targetCurrency);
            
            Map<String, Object> response = new HashMap<>();
            response.put("original", amount);
            response.put("originalCurrency", "GBP");
            response.put("converted", convertedAmount);
            response.put("targetCurrency", targetCurrency);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error converting currency", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to convert currency: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
}