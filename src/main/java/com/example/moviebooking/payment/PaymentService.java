package com.example.moviebooking.payment;
import com.example.moviebooking.booking.*;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service public class PaymentService {
 private final PaymentRepository payments; private final BookingRepository bookings;
 public PaymentService(PaymentRepository payments,BookingRepository bookings){this.payments=payments;this.bookings=bookings;}
 public Payment getForBooking(String bookingId){return payments.findByBookingId(bookingId).orElseThrow(()->new ApiException(404,"PAYMENT_NOT_FOUND","Payment not found"));}
 @Transactional public Payment verify(String bookingId,VerifyPaymentRequest r){var p=getForBooking(bookingId);p.provider=r.provider;p.providerReference=r.providerReference;p.status=PaymentStatus.VERIFIED;var b=bookings.findById(bookingId).orElseThrow(()->new ApiException(404,"BOOKING_NOT_FOUND","Booking not found"));if(b.confirmationStatus!=ConfirmationStatus.PENDING)b.status=BookingStatus.CONFIRMED;return p;}
}
