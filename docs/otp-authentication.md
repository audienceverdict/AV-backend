# Registration and email OTP login

Registration verifies a mobile through WhatsApp and an email through SMTP. Every later login uses email only. The server selects the channel by operation; clients cannot select WhatsApp for login. `AUTH_OTP_CHANNEL` is deprecated and no longer changes routing. Both WhatsApp and SMTP must be configured.

Base path: `/api/v1/auth/otp`. These endpoints are public and accept JSON without an Authorization header.

## API contract

| Method | Path | Purpose |
|---|---|---|
| GET | `/config` | Returns `channel: EMAIL`, `loginChannel: EMAIL`, `registrationChannel: WHATSAPP`, `registrationEmailVerificationRequired: "true"` |
| POST | `/request` | Email login OTP for an existing account |
| POST | `/verify` | Email login verification; returns profile and JWT |
| POST | `/registration/request` | WhatsApp OTP for a new mobile |
| POST | `/registration/verify` | Mobile verification; returns mobile `registrationToken` |
| POST | `/registration/email/request` | Email OTP bound to the mobile registration token |
| POST | `/registration/email/verify` | Email verification; returns email proof in `registrationToken` |
| POST | `/register` | Creates the account after both proofs; returns profile and JWT |

## Registration

1. Request WhatsApp OTP: `POST /registration/request`

   ```json
   {"destination":"9000000001"}
   ```

2. Verify WhatsApp OTP: `POST /registration/verify`

   ```json
   {"destination":"9000000001","otp":"123456"}
   ```

   Save the response's `registrationToken` as the **mobile proof**. This response does not create an account or issue an access token.

3. Request email OTP: `POST /registration/email/request`

   ```json
   {"registrationToken":"<mobile-proof>","email":"viewer@example.com"}
   ```

4. Verify email OTP: `POST /registration/email/verify`

   ```json
   {"registrationToken":"<mobile-proof>","email":"viewer@example.com","otp":"654321"}
   ```

   Save this response's `registrationToken` as the **email proof**. Keep the original mobile proof too.

5. Create the account: `POST /register`

   ```json
   {
     "registrationToken":"<mobile-proof>",
     "emailVerificationToken":"<email-proof>",
     "name":"Viewer",
     "email":"viewer@example.com",
     "mobile":"9000000001"
   }
   ```

   Both proofs must match the contacts and the same registration. Success consumes both challenges and returns `registrationRequired:false`, `user`, `accessToken`, and `tokenType:Bearer`. New accounts have `mobileVerifiedAt` and `emailVerifiedAt` set. Duplicate contacts, reserved admin contacts, and account merging are rejected.

## Returning-user login

1. `POST /request`: `{"destination":"viewer@example.com"}`
2. `POST /verify`: `{"destination":"viewer@example.com","otp":"123456"}`

Success returns the same existing account ID, profile, and JWT. Unknown accounts receive `400 REGISTRATION_REQUIRED` and must register. Disabled accounts and administrators using unauthorized email identities cannot log in. Login has no WhatsApp fallback. A registered mobile cannot request WhatsApp registration OTP again.

Use the JWT for protected endpoints, for example `GET /api/v1/auth/me` with `Authorization: Bearer <accessToken>`.

## Environment and deployment

Configure existing SMTP, database and JWT settings plus:

```dotenv
AUTH_OTP_EXPIRATION_SECONDS=600
APP_ADMIN_MOBILE=+919000000000
WHATSAPP_PHONE_NUMBER_ID=<sender-id>
WHATSAPP_ACCESS_TOKEN=<secret>
WHATSAPP_API_VERSION=v25.0
WHATSAPP_OTP_TEMPLATE=otp
WHATSAPP_TEMPLATE_LANGUAGE=<approved-language-code>
```

Configure `app.admin.email` for the intended administrator. WhatsApp credentials are now required in every mode because registration always uses WhatsApp. Use the approved Authentication template with one body parameter and a Copy code button at index 0. An accepted Meta request does not confirm handset delivery.

The current profiles use Hibernate `ddl-auto: update`, which adds nullable `users.mobile_verified_at`, `users.email_verified_at`, and `otp_verifications.registration_binding` columns on startup. If schema changes are managed separately in your deployment, add equivalent nullable timestamp columns and a nullable VARCHAR(4096) binding column before starting the new backend. Existing verification timestamps remain null; no historical verification is assumed.

Existing accounts with usable email can log in immediately; successful email OTP marks that email verified while preserving their account ID. Accounts with missing or incorrect email need an administrator-assisted contact correction with identity verification; no public email-replacement/recovery endpoint is introduced. In production, provisioning uses the configured admin email and links it to a matching existing ADMIN mobile when the contacts are consistent. Conflicting configured admin contacts fail startup rather than creating a second identity.

Deploy frontend changes with this backend: login always displays email, while registration uses the staged flow above. The previous `/request` mobile behavior and single-proof `/register` contract are replaced. Legacy `/api/v1/auth/email-otp/*` routes return `409 OTP_CHANNEL_DISABLED` in every mode to prevent email-only registration bypass. Outstanding old registration tokens must be discarded and registration restarted. Existing access tokens keep their normal lifetime.

## Limits and verification

OTP hashes, expiry, 30-second default cooldown, hourly request limits, database locks and attempt limits remain in use. Login email and registration email challenges use separate namespaces, and registration email proof is bound to the mobile proof. Resending invalidates the previous proof for that contact and purpose. Registration binding stores a SHA-256 digest, not the registration token. WhatsApp delivery request failures roll back the new challenge. Emails are queued after transaction commit; SMTP or queue failure can occur after a successful API response and requires resending after the cooldown. Failed code attempts commit to retain guessing limits.

Tests cover both-contact registration, mismatched proofs, token reuse, expiry, disabled/unknown accounts, email normalization, unchanged account identity, delivery failures, provider payloads and public HTTP routes. Providers are mocked; automated tests do not send real WhatsApp messages or emails.
