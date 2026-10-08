package com.example.moviebooking.auth.dto;

public record EmailOtpResult(boolean registrationRequired, UserResponse user, String accessToken, String tokenType) {}
