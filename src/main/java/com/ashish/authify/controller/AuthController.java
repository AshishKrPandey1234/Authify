package com.ashish.authify.controller;

import com.ashish.authify.dto.AuthResponse;
import com.ashish.authify.dto.LoginRequest;
import com.ashish.authify.dto.RegisterRequest;
import com.ashish.authify.dto.VerifyRequest;
import com.ashish.authify.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1. ENDPOINT FOR USER REGISTRATION
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody RegisterRequest request) {
        String response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    // 2. ENDPOINT FOR EMAIL OTP VERIFICATION
    @PostMapping("/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody VerifyRequest request) {
        String response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    // 3. ENDPOINT FOR LOGIN (RETURNS JWT)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginUser(request);
        return ResponseEntity.ok(response);
    }
}