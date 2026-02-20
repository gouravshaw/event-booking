package com.example.externalapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Service
public class WikipediaService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    // Get basic information about a city from Wikipedia
    public Map<String, Object> getCityInfo(String cityName) {
        Map<String, Object> cityInfo = new HashMap<>();
        
        try {
            // Clean up city name for URL
            String encodedCity = cityName.replace(" ", "_");
            
            // Use Wikipedia's simple summary API
            String apiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encodedCity;
            
            // Make the request
            String response = restTemplate.getForObject(apiUrl, String.class);
            JSONObject data = new JSONObject(response);
            
            // Just get the basic information we need
            cityInfo.put("name", data.getString("title"));
            cityInfo.put("description", data.getString("extract"));
            
            // Get the thumbnail if available
            if (data.has("thumbnail")) {
                cityInfo.put("imageUrl", data.getJSONObject("thumbnail").getString("source"));
            }
            
        } catch (Exception e) {
            // Simple error handling
            cityInfo.put("error", "Could not find information for this city");
        }
        
        return cityInfo;
    }
}