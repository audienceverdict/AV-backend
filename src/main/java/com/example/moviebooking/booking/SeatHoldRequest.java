package com.example.moviebooking.booking;
import jakarta.validation.constraints.*;
import java.util.*;
public class SeatHoldRequest { @NotBlank public String showId; @NotEmpty public List<String> seatIds; }
