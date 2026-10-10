# Ordered Postman endpoint list

Import the collection and environment. Follow [README.md](README.md) for setup and manual OTP steps.

| Step | Request | Method | URL | Token |
| --- | --- | --- | --- | --- |
| 1 | Request new user email OTP | POST | `{{baseUrl}}/api/v1/auth/email-otp/request` | Public |
| 2 | Verify new user OTP | POST | `{{baseUrl}}/api/v1/auth/email-otp/verify` | Public |
| 3 | Register verified new profile and save bearer token | POST | `{{baseUrl}}/api/v1/auth/email-otp/register` | Public |
| 4 | Get current profile | GET | `{{baseUrl}}/api/v1/auth/me` | userToken |
| 5 | Update current profile | PUT | `{{baseUrl}}/api/v1/auth/me` | userToken |
| 6 | Request configured admin OTP | POST | `{{baseUrl}}/api/v1/auth/email-otp/request` | Public |
| 7 | Verify admin OTP and save admin bearer token | POST | `{{baseUrl}}/api/v1/auth/email-otp/verify` | Public |
| 8 | Request existing user OTP | POST | `{{baseUrl}}/api/v1/auth/email-otp/request` | Public |
| 9 | Verify existing user and save user bearer token | POST | `{{baseUrl}}/api/v1/auth/email-otp/verify` | Public |
| 10 | List users | GET | `{{baseUrl}}/api/v1/admin/users?page=0&size=100` | adminToken |
| 11 | Get sample user | GET | `{{baseUrl}}/api/v1/admin/users/{{targetUserId}}` | adminToken |
| 12 | Disable sample user | PATCH | `{{baseUrl}}/api/v1/admin/users/{{targetUserId}}/status` | adminToken |
| 13 | Enable sample user | PATCH | `{{baseUrl}}/api/v1/admin/users/{{targetUserId}}/status` | adminToken |
| 14 | Set sample user role ADMIN | PATCH | `{{baseUrl}}/api/v1/admin/users/{{targetUserId}}/role` | adminToken |
| 15 | Set sample user role USER | PATCH | `{{baseUrl}}/api/v1/admin/users/{{targetUserId}}/role` | adminToken |
| 16 | Create movie | POST | `{{baseUrl}}/api/v1/movies` | adminToken |
| 17 | List active movies with pagination | GET | `{{baseUrl}}/api/v1/movies?status=ACTIVE&page=0&size=100` | Public |
| 18 | Get movie | GET | `{{baseUrl}}/api/v1/movies/{{movieId}}` | Public |
| 19 | Update movie | PUT | `{{baseUrl}}/api/v1/movies/{{movieId}}` | adminToken |
| 20 | Create theatre | POST | `{{baseUrl}}/api/v1/theatres` | adminToken |
| 21 | List theatres | GET | `{{baseUrl}}/api/v1/theatres?page=0&size=100` | Public |
| 22 | Get theatre | GET | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | Public |
| 23 | Update theatre | PUT | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | adminToken |
| 24 | Create screen with seats | POST | `{{baseUrl}}/api/v1/theatres/{{theatreId}}/screens` | adminToken |
| 25 | Update screen before it is locked | PUT | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}` | adminToken |
| 26 | List screens and capture legacy seat IDs | GET | `{{baseUrl}}/api/v1/theatres/{{theatreId}}/screens` | Public |
| 27 | List layout versions | GET | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions` | Public |
| 28 | Create draft layout | POST | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions?name=Postman%20Layout` | adminToken |
| 29 | Replace draft layout seats | PUT | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/seats` | adminToken |
| 30 | Get layout details | GET | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}` | Public |
| 31 | Publish layout | POST | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/publish` | adminToken |
| 32 | Create show | POST | `{{baseUrl}}/api/v1/shows` | adminToken |
| 33 | List shows with filters | GET | `{{baseUrl}}/api/v1/shows?movieId={{movieId}}&theatreId={{theatreId}}&date={{showDate}}` | Public |
| 34 | Get show | GET | `{{baseUrl}}/api/v1/shows/{{showId}}` | Public |
| 35 | Update show | PUT | `{{baseUrl}}/api/v1/shows/{{showId}}` | adminToken |
| 36 | Get availability | GET | `{{baseUrl}}/api/v1/shows/{{showId}}/availability` | Public |
| 37 | Hold seat | POST | `{{baseUrl}}/api/v1/bookings/seat-holds` | userToken |
| 38 | Get active holds | GET | `{{baseUrl}}/api/v1/bookings/seat-holds?showId={{showId}}` | userToken |
| 39 | Create paid booking | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 40 | Get my bookings | GET | `{{baseUrl}}/api/v1/bookings/me` | userToken |
| 41 | Get my booking | GET | `{{baseUrl}}/api/v1/bookings/{{bookingId}}` | userToken |
| 42 | Get booking payment | GET | `{{baseUrl}}/api/v1/payments/booking/{{bookingId}}` | userToken |
| 43 | Verify payment | POST | `{{baseUrl}}/api/v1/payments/booking/{{bookingId}}/verify` | userToken |
| 44 | Admin list bookings | GET | `{{baseUrl}}/api/v1/bookings/admin` | adminToken |
| 45 | Admin confirm booking | POST | `{{baseUrl}}/api/v1/bookings/admin/{{bookingId}}/confirm` | adminToken |
| 46 | Mark attended | POST | `{{baseUrl}}/api/v1/bookings/admin/{{bookingId}}/attendance?attended=true` | adminToken |
| 47 | Create review | POST | `{{baseUrl}}/api/v1/reviews` | userToken |
| 48 | Update my review | PUT | `{{baseUrl}}/api/v1/reviews/{{reviewId}}` | userToken |
| 49 | Moderate and highlight review | POST | `{{baseUrl}}/api/v1/reviews/admin/{{reviewId}}/moderate?status=APPROVED&highlighted=true` | adminToken |
| 50 | List approved reviews | GET | `{{baseUrl}}/api/v1/reviews?movieId={{movieId}}&status=APPROVED` | Public |
| 51 | Send show-booker campaign | POST | `{{baseUrl}}/api/v1/notifications/admin/campaigns` | adminToken |
| 52 | Send show reminder | POST | `{{baseUrl}}/api/v1/notifications/admin/shows/{{showId}}/reminder` | adminToken |
| 53 | Get my notifications | GET | `{{baseUrl}}/api/v1/notifications` | userToken |
| 54 | Join sold-out seeded show waiting list | POST | `{{baseUrl}}/api/v1/waiting-list` | userToken |
| 55 | List my waiting entries | GET | `{{baseUrl}}/api/v1/waiting-list` | userToken |
| 56 | Admin list waiting entries | GET | `{{baseUrl}}/api/v1/waiting-list/admin` | adminToken |
| 57 | Release seeded booking seats to offer earlier entry | POST | `{{baseUrl}}/api/v1/bookings/admin/{{waitingBookingId}}/cancel` | adminToken |
| 58 | Create second booking for offer trigger | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 59 | Cancel trigger booking to offer my entry | POST | `{{baseUrl}}/api/v1/bookings/{{offerTriggerBookingId}}/cancel` | userToken |
| 60 | Claim waiting list offer | POST | `{{baseUrl}}/api/v1/waiting-list/{{waitingEntryId}}/claim` | userToken |
| 61 | Cancel my waiting entry | DELETE | `{{baseUrl}}/api/v1/waiting-list/{{waitingEntryId}}` | userToken |
| 62 | Cancel my booking | POST | `{{baseUrl}}/api/v1/bookings/{{bookingId}}/cancel` | userToken |
| 63 | Create second booking for admin cancellation | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 64 | Admin cancel booking with reason | POST | `{{baseUrl}}/api/v1/bookings/admin/{{adminCancelBookingId}}/cancel` | adminToken |
| 65 | Cancel show | DELETE | `{{baseUrl}}/api/v1/shows/{{showId}}` | adminToken |
| 66 | Delete screen | DELETE | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}` | adminToken |
| 67 | Delete theatre | DELETE | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | adminToken |
| 68 | Delete movie | DELETE | `{{baseUrl}}/api/v1/movies/{{movieId}}` | adminToken |
