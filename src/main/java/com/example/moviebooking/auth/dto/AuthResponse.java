package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record AuthResponse(UserResponse user, String accessToken, String tokenType) {}
