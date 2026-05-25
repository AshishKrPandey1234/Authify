package com.ashish.authify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/home-content")
    public ResponseEntity<Map<String, String>> getHomeContent() {

        return ResponseEntity.ok(Map.of(
                "status", "AUTHORIZED",
                "message", "Protected resource accessed successfully.",
                "project", "Authify - Secure JWT Authentication System",
                "developer", "Ashish Kumar Pandey",
                "feature", "User authentication and route protection are active."
        ));
    }
}