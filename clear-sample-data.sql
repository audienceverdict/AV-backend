USE movie_booking;

-- Deletes application/sample records but keeps the database schema and Flyway history.
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking_seats;
TRUNCATE TABLE bookings;
TRUNCATE TABLE payments;
TRUNCATE TABLE notifications;
TRUNCATE TABLE reviews;
TRUNCATE TABLE shows;
TRUNCATE TABLE layout_seats;
TRUNCATE TABLE layout_versions;
TRUNCATE TABLE seats;
TRUNCATE TABLE screens;
TRUNCATE TABLE venue_media;
TRUNCATE TABLE theatres;
TRUNCATE TABLE movies;
TRUNCATE TABLE movie_cast;
TRUNCATE TABLE movie_genres;
TRUNCATE TABLE otp_verifications;
TRUNCATE TABLE auth_locks;`r`nTRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

