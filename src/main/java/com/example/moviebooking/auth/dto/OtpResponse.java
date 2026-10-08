package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record OtpResponse(boolean success, String message, long expiresInSeconds, long resendAfterSeconds) {}
