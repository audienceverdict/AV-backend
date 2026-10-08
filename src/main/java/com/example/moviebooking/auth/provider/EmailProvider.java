package com.example.moviebooking.auth.provider;

public interface EmailProvider {
    void send(String to, String subject, String body);
    default void sendHtml(String to, String subject, String plainText, String html, String inlinePosterDataUri) {
        send(to, subject, plainText);
    }
}
