package com.example.externalapi.controller;

import com.example.externalapi.service.WikipediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/city")
@CrossOrigin(origins = "*")
public class WikipediaController {
    
    @Autowired
    private WikipediaService wikipediaService;
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getCityInfo(@RequestParam String name) {
        Map<String, Object> cityInfo = wikipediaService.getCityInfo(name);
        return ResponseEntity.ok(cityInfo);
    }
}