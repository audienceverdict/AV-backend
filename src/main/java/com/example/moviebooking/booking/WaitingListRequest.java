package com.example.moviebooking.booking;
import jakarta.validation.constraints.*;
public class WaitingListRequest { @NotBlank public String showId; @Min(1) public int requestedSeatsCount=1; }
