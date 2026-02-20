package com.example.externalapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    
    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "https://api.frankfurter.app";
    
    @Autowired
    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get the latest exchange rates for GBP
     */
    @Cacheable("exchangeRates")
    public Map<String, Object> getExchangeRates() {
        try {
            logger.info("Fetching exchange rates from Frankfurter API");
            String url = API_BASE_URL + "/latest?from=GBP";
            
            // Make the API request
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            logger.info("Successfully retrieved exchange rates");
            return response;
        } catch (Exception e) {
            logger.error("Error fetching exchange rates", e);
            return new HashMap<>();
        }
    }
    
    /**
     * Convert an amount from GBP to the target currency
     */
    public double convertCurrency(double amount, String targetCurrency) {
        try {
            if (targetCurrency.equals("GBP")) {
                return amount; // No conversion needed
            }
            
            Map<String, Object> rates = getExchangeRates();
            logger.info("Converting {} GBP to {}", amount, targetCurrency);
            
            if (rates.containsKey("rates")) {
                Map<String, Double> ratesMap = (Map<String, Double>) rates.get("rates");
                
                if (ratesMap.containsKey(targetCurrency)) {
                    double rate = ratesMap.get(targetCurrency);
                    double converted = amount * rate;
                    logger.info("Converted amount: {} {}", converted, targetCurrency);
                    return converted;
                }
            }
            
            logger.error("Currency not found: {}", targetCurrency);
            return amount; // Return original amount if conversion fails
        } catch (Exception e) {
            logger.error("Error converting currency", e);
            return amount; // Return original amount if conversion fails
        }
    }
    
    /**
     * Get a list of available currencies
     */
    @Cacheable("currencies")
    public Map<String, String> getAvailableCurrencies() {
        Map<String, String> currencies = new HashMap<>();
        
        // Add base currency
        currencies.put("GBP", "British Pound");
        
        // Common currencies to include in the demo
        currencies.put("USD", "US Dollar");
        currencies.put("EUR", "Euro");
        currencies.put("JPY", "Japanese Yen");
        currencies.put("CAD", "Canadian Dollar");
        currencies.put("AUD", "Australian Dollar");
        currencies.put("CHF", "Swiss Franc");
        currencies.put("CNY", "Chinese Yuan");
        currencies.put("INR", "Indian Rupee");
        currencies.put("BRL", "Brazilian Real");
        currencies.put("MXN", "Mexican Peso");
        
        logger.info("Returning {} available currencies", currencies.size());
        return currencies;
    }
}