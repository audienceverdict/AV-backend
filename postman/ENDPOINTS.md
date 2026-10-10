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
| 20 | Search enhanced movie catalog | GET | `{{baseUrl}}/api/v1/movies?title=Postman&genre=Drama&language=Telugu&page=0&size=20` | Public |
| 21 | Create reusable actor and technician | POST | `{{baseUrl}}/api/v1/people` | adminToken |
| 22 | Search people | GET | `{{baseUrl}}/api/v1/people?name=Postman&page=0&size=20` | Public |
| 23 | Get person profile | GET | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}` | Public |
| 24 | Update technician profile | PUT | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}` | adminToken |
| 25 | Add social profile | POST | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/social-links` | adminToken |
| 26 | List social profiles | GET | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/social-links` | Public |
| 27 | Update social profile | PUT | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/social-links/{{catalogSocialId}}` | adminToken |
| 28 | Add actor and character | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits` | adminToken |
| 29 | Update actor character | PUT | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits/{{catalogCreditId}}` | adminToken |
| 30 | Add crew role Film editor | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits` | adminToken |
| 31 | Add crew role VFX supervisor | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits` | adminToken |
| 32 | Get movie credits | GET | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits` | Public |
| 33 | Get movie cast | GET | `{{baseUrl}}/api/v1/movies/{{movieId}}/cast` | Public |
| 34 | Get movie crew | GET | `{{baseUrl}}/api/v1/movies/{{movieId}}/crew` | Public |
| 35 | Get person filmography | GET | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/filmography?page=0&size=20` | Public |
| 36 | Get distinct credited movies | GET | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/movies?page=0&size=20` | Public |
| 37 | Add primary theatrical poster | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 38 | Add labeled video TRAILER | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 39 | Add labeled video TEASER | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 40 | Add labeled video MAKING_OF | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 41 | Add labeled video INTERVIEW | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 42 | Add labeled video EVENT_VIDEO | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 43 | Add labeled movie still | POST | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | adminToken |
| 44 | Update media label | PUT | `{{baseUrl}}/api/v1/movies/{{movieId}}/media/{{catalogMediaId}}` | adminToken |
| 45 | Select primary poster | PUT | `{{baseUrl}}/api/v1/movies/{{movieId}}/media/{{catalogPosterId}}/primary` | adminToken |
| 46 | List movie media and capture order | GET | `{{baseUrl}}/api/v1/movies/{{movieId}}/media` | Public |
| 47 | Reorder movie media | PUT | `{{baseUrl}}/api/v1/movies/{{movieId}}/media/order` | adminToken |
| 48 | Delete sample movie still | DELETE | `{{baseUrl}}/api/v1/movies/{{movieId}}/media/{{catalogMediaId}}` | adminToken |
| 49 | Delete sample cast credit | DELETE | `{{baseUrl}}/api/v1/movies/{{movieId}}/credits/{{catalogCreditId}}` | adminToken |
| 50 | Delete sample social link | DELETE | `{{baseUrl}}/api/v1/people/{{catalogPersonId}}/social-links/{{catalogSocialId}}` | adminToken |
| 51 | Create theatre | POST | `{{baseUrl}}/api/v1/theatres` | adminToken |
| 52 | List theatres | GET | `{{baseUrl}}/api/v1/theatres?page=0&size=100` | Public |
| 53 | Get theatre | GET | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | Public |
| 54 | Update theatre | PUT | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | adminToken |
| 55 | Create screen with seats | POST | `{{baseUrl}}/api/v1/theatres/{{theatreId}}/screens` | adminToken |
| 56 | Update screen before it is locked | PUT | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}` | adminToken |
| 57 | List screens and capture legacy seat IDs | GET | `{{baseUrl}}/api/v1/theatres/{{theatreId}}/screens` | Public |
| 58 | List layout versions | GET | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions` | Public |
| 59 | Create draft layout | POST | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions?name=Postman%20Layout` | adminToken |
| 60 | Replace draft layout seats | PUT | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/seats` | adminToken |
| 61 | Get layout details | GET | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}` | Public |
| 62 | Publish layout | POST | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/publish` | adminToken |
| 63 | Create show | POST | `{{baseUrl}}/api/v1/shows` | adminToken |
| 64 | List shows with filters | GET | `{{baseUrl}}/api/v1/shows?movieId={{movieId}}&theatreId={{theatreId}}&date={{showDate}}` | Public |
| 65 | Get show | GET | `{{baseUrl}}/api/v1/shows/{{showId}}` | Public |
| 66 | Update show | PUT | `{{baseUrl}}/api/v1/shows/{{showId}}` | adminToken |
| 67 | Get availability | GET | `{{baseUrl}}/api/v1/shows/{{showId}}/availability` | Public |
| 68 | Hold seat | POST | `{{baseUrl}}/api/v1/bookings/seat-holds` | userToken |
| 69 | Get active holds | GET | `{{baseUrl}}/api/v1/bookings/seat-holds?showId={{showId}}` | userToken |
| 70 | Create paid booking | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 71 | Get my bookings | GET | `{{baseUrl}}/api/v1/bookings/me` | userToken |
| 72 | Get my booking | GET | `{{baseUrl}}/api/v1/bookings/{{bookingId}}` | userToken |
| 73 | Get booking payment | GET | `{{baseUrl}}/api/v1/payments/booking/{{bookingId}}` | userToken |
| 74 | Verify payment | POST | `{{baseUrl}}/api/v1/payments/booking/{{bookingId}}/verify` | userToken |
| 75 | Admin list bookings | GET | `{{baseUrl}}/api/v1/bookings/admin` | adminToken |
| 76 | Admin confirm booking | POST | `{{baseUrl}}/api/v1/bookings/admin/{{bookingId}}/confirm` | adminToken |
| 77 | Mark attended | POST | `{{baseUrl}}/api/v1/bookings/admin/{{bookingId}}/attendance?attended=true` | adminToken |
| 78 | Create review | POST | `{{baseUrl}}/api/v1/reviews` | userToken |
| 79 | Update my review | PUT | `{{baseUrl}}/api/v1/reviews/{{reviewId}}` | userToken |
| 80 | Moderate and highlight review | POST | `{{baseUrl}}/api/v1/reviews/admin/{{reviewId}}/moderate?status=APPROVED&highlighted=true` | adminToken |
| 81 | List approved reviews | GET | `{{baseUrl}}/api/v1/reviews?movieId={{movieId}}&status=APPROVED` | Public |
| 82 | Send show-booker campaign | POST | `{{baseUrl}}/api/v1/notifications/admin/campaigns` | adminToken |
| 83 | Send show reminder | POST | `{{baseUrl}}/api/v1/notifications/admin/shows/{{showId}}/reminder` | adminToken |
| 84 | Get my notifications | GET | `{{baseUrl}}/api/v1/notifications` | userToken |
| 85 | Join sold-out seeded show waiting list | POST | `{{baseUrl}}/api/v1/waiting-list` | userToken |
| 86 | List my waiting entries | GET | `{{baseUrl}}/api/v1/waiting-list` | userToken |
| 87 | Admin list waiting entries | GET | `{{baseUrl}}/api/v1/waiting-list/admin` | adminToken |
| 88 | Release seeded booking seats to offer earlier entry | POST | `{{baseUrl}}/api/v1/bookings/admin/{{waitingBookingId}}/cancel` | adminToken |
| 89 | Create second booking for offer trigger | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 90 | Cancel trigger booking to offer my entry | POST | `{{baseUrl}}/api/v1/bookings/{{offerTriggerBookingId}}/cancel` | userToken |
| 91 | Claim waiting list offer | POST | `{{baseUrl}}/api/v1/waiting-list/{{waitingEntryId}}/claim` | userToken |
| 92 | Cancel my waiting entry | DELETE | `{{baseUrl}}/api/v1/waiting-list/{{waitingEntryId}}` | userToken |
| 93 | Cancel my booking | POST | `{{baseUrl}}/api/v1/bookings/{{bookingId}}/cancel` | userToken |
| 94 | Create second booking for admin cancellation | POST | `{{baseUrl}}/api/v1/bookings` | userToken |
| 95 | Admin cancel booking with reason | POST | `{{baseUrl}}/api/v1/bookings/admin/{{adminCancelBookingId}}/cancel` | adminToken |
| 96 | Cancel show | DELETE | `{{baseUrl}}/api/v1/shows/{{showId}}` | adminToken |
| 97 | Delete screen | DELETE | `{{baseUrl}}/api/v1/theatres/screens/{{screenId}}` | adminToken |
| 98 | Delete theatre | DELETE | `{{baseUrl}}/api/v1/theatres/{{theatreId}}` | adminToken |
| 99 | Delete movie | DELETE | `{{baseUrl}}/api/v1/movies/{{movieId}}` | adminToken |
