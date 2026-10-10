# AV database fixtures and Postman testing

The local `movie_booking` database has been populated with 100 rows in every entity and master collection table, with 400 rows in `seats`, `layout_seats`, and `booking_seats`. Generated counts and deterministic fixture IDs are in [manifest.json](../sample-data/manifest.json); actual counts after live verification are in [database-counts.json](../sample-data/database-counts.json). Verification added test users and notification/history rows, so some actual counts exceed the seed minimum. Shows are dated 30 days after import. All seeded shows are sold out; Postman creates a separate show for the normal booking flow.

## Start and import

1. Start the backend with your configured local database: `mvn spring-boot:run`. Default API base URL: `http://localhost:8080`.
2. In Postman, import [AV-backend.postman_collection.json](AV-backend.postman_collection.json) and [AV-local.postman_environment.json](AV-local.postman_environment.json).
3. Select **AV local** as the active environment. Set `baseUrl` if your port differs, `registrationEmail` to an unused inbox you control, and `registrationMobile` to an unused 10-digit Indian number. `adminEmail` already matches the local configuration used to generate the seed.
4. Run folder **01** one request at a time: request OTP → copy the emailed code into `userOtp` → verify → register → get/update profile. Registration follows verification. `accessToken` is saved as `userToken`, and protected user requests automatically send `Authorization: Bearer {{userToken}}`.
5. Run folder **02**: request the admin OTP, set `adminOtp`, verify. The collection saves `adminToken`. Admin requests use `Authorization: Bearer {{adminToken}}`. There is no password login, refresh-token endpoint, or server-side logout endpoint.
6. Skip optional folder **03** when testing a newly registered user. This folder demonstrates existing-user sign-in: request OTP → set `existingUserOtp` → verify → receive token without registration. Seeded `example.test` addresses need local email logging; they are not deliverable inboxes.
7. Run folders **04–09** in order. They create master data and capture movie, theatre, screen, layout, show, seat, booking, and review IDs automatically. `showDate` defaults to 31 days in the future. Campaign and reminder requests send email to the test show's booker.
8. Run folder **10** using the newly registered user from folder 01. Join the sold-out seeded show, release its seeded booking, create/cancel a trigger booking, then claim the offer within five minutes. The seeded earlier entry receives the first offer; cancelling the trigger booking offers seats to your entry. Claiming an offer does not create a booking; use `POST /bookings` separately to reserve seats. This scenario changes the last seeded booking and waiting-list entries and is intended to run once per fixture import.
9. Run folder **11** last to test user/admin cancellation and delete the Postman-created master data. These requests change/delete data created by the collection.

[ENDPOINTS.md](ENDPOINTS.md) lists all 68 step-by-step request URLs. All 58 distinct controller method/path combinations are represented; some endpoints appear several times to test different actions. Tests check expected HTTP status. OTP steps require manual entry, so do not run the entire collection unattended. A token expires according to the backend JWT configuration; repeat login when necessary. OTPs expire in five minutes by default, with a 30-second resend cooldown.

## Local email logging

To test fictional fixture addresses without sending email, start with the `dev` profile and override the SMTP host to empty. Keep your local datasource configured through environment variables:

```sh
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --spring.flyway.enabled=false --spring.mail.host= --spring.mail.username= --spring.mail.password="
```

Read each OTP from the backend's `LOCAL DEVELOPMENT ONLY` email log and copy it into the corresponding Postman variable. Do not use the dev profile for deployment. The currently configured `upen` profile uses SMTP; logging requires the explicit override above.

## Regenerate or import into another local database

Fixtures are explicit development inserts; the SQL does not delete existing data. The import is transactional and a duplicate fixture ID/email/mobile rolls it back. Do not re-import into the already seeded database. Use a fresh development schema for another complete run.

```sh
python3 scripts/generate_sample_data.py --count 100 --admin-email admin@example.test
python3 scripts/generate_postman.py
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/av-seed-classpath.txt
export SEED_DB_URL='jdbc:mysql://localhost:3306/movie_booking'
export SEED_DB_USER='your-local-user'
read -s SEED_DB_PASSWORD
export SEED_DB_PASSWORD
java -cp "$(cat /tmp/av-seed-classpath.txt)" scripts/ImportSampleData.java sample-data/seed.sql
```

Start the application once before import to create its schema. Make `--admin-email` match `app.admin.email`. Set the environment credentials to the intended local database. `--count` accepts 100 or more; regenerate both artifacts together. Auth locks include the 64 buckets used by the service plus inert IDs to satisfy the requested 100-row minimum. OTP fixtures are expired history, not usable login codes; scheduled cleanup removes them after the retention period. Expired seat holds intentionally leave seats available when bookings are cancelled.

## Current API limitations exposed by testing

- Seat holds validate legacy screen seat IDs, while availability exposes version seat IDs. The collection captures legacy IDs and uses the booking service's legacy fallback for the normal flow. Seeded layout seats share legacy IDs so waiting-list availability stays consistent.
- Payment verification currently accepts provider/reference fields directly. The collection tests the implemented behavior.
- Movie deletion now deletes poster-image rows before the movie, allowing the cleanup workflow to complete. Plain-text OTP emails now include the code, enabling development log verification.
- Cancelling shows or deleting screens/theatres does not clean up all seat-hold/waiting-list history. The collection does not create waiting entries against its temporary show.

## Verification performed

`mvn package` passed, including the plain-text OTP regression test. A local dev server on port 18081 executed all 66 main-workflow HTTP requests from the collection with email logging; all returned the expected 200 status. The optional existing-user folder was skipped. The verification harness mirrored environment capture in Python; this was not a Postman/Newman runner execution. Results are in [verification-results.json](verification-results.json). The temporary server was stopped and the seeded sold-out booking/waiting scenario restored afterward.
