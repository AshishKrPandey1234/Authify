package com.ashish.authify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyRequest {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "OTP cannot be empty")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otp;
}