package com.example.moviebooking.auth.dto;
import jakarta.validation.constraints.*;
public record RoleUpdateRequest(@NotNull com.example.moviebooking.auth.entity.Role role) {}
