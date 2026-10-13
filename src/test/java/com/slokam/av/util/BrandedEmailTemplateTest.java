package com.slokam.av.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BrandedEmailTemplateTest {
    @Test
    void otpIsAvailableToPlainTextClientsAndDevelopmentLogs() {
        var content = BrandedEmailTemplate.otp("012345", 5);
        assertTrue(content.plainText().contains("012345"));
        assertTrue(content.plainText().contains("5 minutes"));
        assertTrue(content.html().contains("012345"));
    }
    @Test
    void seatLabelsAppearForEveryBookingStatusAndFullCancellation() {
        var booking = new com.slokam.av.entity.Booking();
        booking.seatIds = java.util.List.of("internal-seat-id");
        for (var status : com.slokam.av.entity.BookingStatus.values()) {
            booking.status = status;
            var content = BrandedEmailTemplate.ticket("User", booking, null,
                    java.util.List.of("A1", "A2"), java.util.List.of());
            assertTrue(content.plainText().contains("Seats: A1, A2"));
            assertTrue(content.html().contains("A1, A2"));
            org.junit.jupiter.api.Assertions.assertFalse(content.plainText().contains("internal-seat-id"));
        }
        booking.status = com.slokam.av.entity.BookingStatus.CANCELLED;
        booking.seatIds = java.util.List.of();
        var cancelled = BrandedEmailTemplate.ticket("User", booking, null,
                java.util.List.of(), java.util.List.of("A1", "A2"));
        assertTrue(cancelled.plainText().contains("Cancelled seats: A1, A2"));
        assertTrue(cancelled.html().contains("Cancelled seats"));
        assertTrue(cancelled.html().contains("A1, A2"));
    }
}
