package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record AccountStatusRequest(@NotNull Boolean enabled) {}
