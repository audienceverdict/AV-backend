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
}
