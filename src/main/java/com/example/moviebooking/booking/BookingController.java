package com.example.moviebooking.booking;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
@RestController @RequestMapping("/api/v1/bookings")
public class BookingController {
 private final BookingService service;
 public BookingController(BookingService service){this.service=service;}
 @GetMapping("/me") public List<Booking> mine(Principal p){return service.mine(p);}
 @PostMapping("/seat-holds") public List<SeatHold> hold(Principal p,@Valid @RequestBody SeatHoldRequest r){return service.hold(p,r);}
 @GetMapping("/seat-holds") public List<SeatHold> activeHolds(Principal p,@RequestParam String showId){return service.activeHolds(p,showId);}
 @GetMapping("/admin") public List<Booking> all(){return service.all();}
 @GetMapping("/{id}") public Booking get(@PathVariable String id,Principal p){return service.get(id,p);}
 @PostMapping public Booking create(Principal p,@Valid @RequestBody CreateBookingRequest r){return service.create(p,r);}
 @PostMapping("/{id}/cancel") public Booking cancel(@PathVariable String id,Principal p,@RequestBody(required=false) CancelBookingRequest request){return service.cancel(id,p,request==null?null:request.seatIds());}
 @PostMapping("/admin/{id}/confirm") public Booking confirm(@PathVariable String id){return service.adminConfirm(id);}
 @PostMapping("/admin/{id}/cancel") public Booking adminCancel(@PathVariable String id,@Valid @RequestBody AdminCancellationRequest request){return service.adminCancel(id,request.reason(),request.seatIds());}
 @PostMapping("/admin/{id}/attendance") public Booking attended(@PathVariable String id,@RequestParam boolean attended){return service.markAttended(id,attended);}
}
