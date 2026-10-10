# Authentication backend

Java 21, Maven, Spring Boot 3.4.13, MySQL 8. Flyway versions the movie catalog schema and legacy backfill. Existing profiles retain `ddl-auto=update` for unrelated modules; see [catalog migration and API guide](docs/movie-catalog.md) before deployment.

## Local configuration (PowerShell)

Use a dedicated local MySQL user with permission to create the database. The backend JDBC URL includes `createDatabaseIfNotExist=true`, so MySQL creates `movie_booking` automatically on first startup when the user has enough privileges.

```sql
CREATE USER 'movie_user'@'localhost' IDENTIFIED BY '<choose-a-local-password>';
GRANT ALL PRIVILEGES ON *.* TO 'movie_user'@'localhost';
FLUSH PRIVILEGES;
```

Local MySQL connection:

```text
Host: localhost
Port: 3306
Database: movie_booking
Username: movie_user
Password: your locally configured database password
JDBC URL: jdbc:mysql://localhost:3306/movie_booking?createDatabaseIfNotExist=true
```

Configure local settings in `src/main/resources/application.yml`. Set the datasource URL, username, and password for your MySQL instance. Set SMTP username, password, and sender address to enable email OTP delivery; with the `dev` profile and SMTP left unconfigured, OTP email content is logged locally.

```powershell
mvn spring-boot:run
```

The `application.yml` file contains all backend settings, including SMTP options. Replace each `CHANGE_ME` value with the appropriate local or deployment value before starting the application. Keep real credentials out of source control; for production, inject or mount a deployment-specific `application.yml` with restricted access. `jwt.secret` must contain at least 32 bytes and remain stable across restarts. The SMTP username and sender address should be the same Gmail address, and the SMTP password should be a Google App Password.

Leave `app.admin.mobile` and `app.admin.email` empty for user-only development. When configured together under `dev`, startup creates/promotes that account to ADMIN. Admin sign-in uses the configured email OTP. The seed never runs without the dev profile.

Email OTP is the only sign-in method. In the development profile, email content is logged locally only when SMTP is not configured. Never enable `dev` in production. The configured `ADMIN_EMAIL` must be linked to an enabled account with the `ADMIN` role in production; the development-only admin seed is not enabled there. Admin OTP verification rejects the configured email if it is not an existing enabled ADMIN account, rejects other ADMIN accounts, and never permits public registration of the reserved admin address. Ordinary USER accounts continue to use the same public email OTP endpoints and `{email}` request format.

## Email OTP

`POST /api/v1/auth/email-otp/request` sends a one-time code to an address. Codes remain BCrypt-hashed in the OTP database, expire under `auth.otp.expiration-seconds`, and follow the persisted resend/hourly rate limits. After code verification, new non-admin accounts must submit their name and mobile number to `/api/v1/auth/email-otp/register` before receiving a session. Existing complete accounts sign in after email verification. Unexpected request/verification/delivery errors are logged server-side and return generic messages; SMTP connection/authentication timeouts are configured in `spring.mail.properties` and default to 10 seconds.

On Hostinger, provide a production `application.yml` with the deployment credentials, verify the service uses the production profile (not `dev`), deploy the updated jar and configuration, and restart/redeploy the application. Confirm the configured admin user already exists with `ADMIN` role, then request an OTP and inspect Hostinger application logs for SMTP diagnostics if delivery fails. Do not place backend secrets in frontend configuration or commit them to Git.

Keep `jwt.secret` stable across restarts. Configure `app.cors-origins` as comma-separated exact allowed origins. Create the first production administrator through your controlled database/deployment procedure; the development seed is disabled outside `dev`.

## API

| Method | Endpoint | Body / access |
| --- | --- | --- |
| POST | `/api/v1/auth/email-otp/request` | `{email}`; public |
| POST | `/api/v1/auth/email-otp/verify` | `{email, otp}`; public |
| POST | `/api/v1/auth/email-otp/register` | `{email, name, mobile}` after OTP verification; public |
| GET | `/api/v1/auth/me` | Bearer token |
| PUT | `/api/v1/auth/me` | `{name, email}`; Bearer token |
| GET | `/api/v1/admin/users?page=0&size=20` | ADMIN; paginated `content` |
| GET | `/api/v1/admin/users/{id}` | ADMIN |
| PATCH | `/api/v1/admin/users/{id}/role` | `{role: "USER" or "ADMIN"}`; ADMIN |
| PATCH | `/api/v1/admin/users/{id}/status` | `{enabled: true or false}`; ADMIN |

Existing verified email accounts receive `{registrationRequired: false, user, accessToken, tokenType}`. New email addresses receive `{registrationRequired: true}` after code verification and must provide a name and mobile number before registration completes. Emails are lowercased and unique.

Mobile defaults to +91 with 10 national digits; configure `MOBILE_COUNTRY_CODE` and `MOBILE_NATIONAL_LENGTH` together if changing country, and update the frontend country label/validation. Spaces, parentheses and hyphens are normalized. Email is the required sign-in identifier. Mobile is collected for new profiles and normalized on registration.

`OTP_EXPIRATION_SECONDS=300`, `OTP_RESEND_COOLDOWN_SECONDS=30`, `OTP_MAX_ATTEMPTS=5`, `OTP_MAX_REQUESTS_PER_HOUR=10`, `JWT_EXPIRATION_MS=3600000` are configurable. OTP request response includes `resendAfterSeconds` for the UI. Rate limits are per email address, persisted in the database. Failed guesses persist their attempt count. Only the latest email OTP is accepted. Cleanup retains history longer than the hourly request window.

JWTs are signed with HS256 and validated for issuer, signature and expiry. The current database account status and role are checked on every protected request, so deactivation and demotion affect existing tokens. Logout clears the browser session; access tokens expire after the configured lifetime. There is no refresh-token or server-side logout endpoint in this scope.

Use edge-level IP/global request limits for public email OTP endpoints.

## Tests

`mvn test` runs endpoint, persistence, concurrency, OTP, profile, JWT, CORS and authorization checks with H2. `mvn package` produces the executable jar in `target`.
"# AV-backend" 

## Sample database and complete Postman collection

See [postman/README.md](postman/README.md) for the populated 100-record-per-entity dataset, import instructions, OTP/token steps, and the complete API workflow. [postman/ENDPOINTS.md](postman/ENDPOINTS.md) lists every request URL.


### Application logging

Logging defaults to INFO. Set `APP_LOG_LEVEL=DEBUG` for service diagnostics or
`APP_LOG_LEVEL=TRACE` for AOP method start/exit logs with outcome and elapsed time.
The aspect covers controllers, services, security, repositories, and future
scheduler/mapper beans. Spring AOP intercepts calls through Spring proxies;
private methods, constructors, and calls within the same bean are not intercepted.

INFO records business state changes; WARN records rejected requests and recoverable
failures; ERROR records server failures. State-change logs describe work performed
inside the method and are not a guarantee that an enclosing transaction committed.
Arguments, return values, JWTs, OTPs, and email bodies are not logged. Unexpected
errors include the exception type and stack frames without the exception message.

Movie catalog enhancements, URL-only media examples, people/credits APIs, and migration instructions: [docs/movie-catalog.md](docs/movie-catalog.md).
