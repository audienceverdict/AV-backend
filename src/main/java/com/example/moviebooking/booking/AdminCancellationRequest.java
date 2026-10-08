package com.example.moviebooking.booking;
import jakarta.validation.constraints.*;
public record AdminCancellationRequest(@NotBlank @Size(max=500) String reason, java.util.List<String> seatIds) {}
