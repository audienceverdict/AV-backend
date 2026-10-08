package com.example.moviebooking.payment;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/payments")
public class PaymentController {
 private final PaymentService service;
 public PaymentController(PaymentService service){this.service=service;}
 @GetMapping("/booking/{bookingId}") public Payment get(@PathVariable String bookingId){return service.getForBooking(bookingId);}
 @PostMapping("/booking/{bookingId}/verify") public Payment verify(@PathVariable String bookingId,@Valid @RequestBody VerifyPaymentRequest r){return service.verify(bookingId,r);}
}
