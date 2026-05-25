package com.ashish.authify.service;

import com.ashish.authify.dto.AuthResponse;
import com.ashish.authify.dto.LoginRequest;
import com.ashish.authify.dto.RegisterRequest;
import com.ashish.authify.dto.VerifyRequest;
import com.ashish.authify.entity.User;
import com.ashish.authify.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, EmailService emailService,
                       JwtService jwtService, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. SIGNUP USER LOGIC
    public String registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered!");
        }

        // Generate 6-Digit random OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt Hashing
                .otp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5)) // Valid for 5 mins
                .enabled(false) // Account is disabled until OTP verification
                .build();

        userRepository.save(user);

        // Dispatch OTP Email asynchronously/back-end
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "Registration successful! Please check your email for the verification OTP.";
    }

    // 2. EMAIL OTP VERIFICATION LOGIC
    public String verifyOtp(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email!"));

        if (user.isEnabled()) {
            return "Account is already verified and active.";
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired! Please request a new one.");
        }

        if (!user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid verification OTP. Access Denied.");
        }

        // Activate the user account
        user.setEnabled(true);
        user.setOtp(null); // Clear OTP field once used
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Account verified successfully! You can now proceed to log in.";
    }

    // 3. LOGIN LOGIC (RETURNS 30-MIN JWT)
    public AuthResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email credentials!"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Please verify your email account using the OTP before logging in.");
        }

        // Verify incoming plain password against stored BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password credentials!");
        }

        // Generate stateless 30-minute JWT Token
        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .message("Login successful!")
                .build();
    }
}