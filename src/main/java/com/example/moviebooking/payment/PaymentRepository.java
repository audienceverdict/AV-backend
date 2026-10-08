package com.example.moviebooking.payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PaymentRepository extends JpaRepository<Payment,String> {
 Optional<Payment> findByBookingId(String bookingId);
}
