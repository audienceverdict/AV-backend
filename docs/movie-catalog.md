# Movie catalog integration

The existing `com.slokam.av` movie module now supports richer profiles, reusable actors and technicians, movie-specific character/crew credits, and multiple labeled media URLs. No catalog endpoint accepts files, multipart bodies, Base64 images, or upload-only fields. No URL validation or embedding code downloads remote media.

## Existing-client compatibility

The existing `/api/v1/movies` CRUD routes, UUID IDs, page response shape, and successful CRUD status codes remain available. Responses retain `duration`, `description`, `genre`, `language`, `cast`, `director`, `production`, `posterUrl`, `posterImages`, `backdropUrl`, `trailerUrl`, `status`, `createdAt`, and `updatedAt`. New requests also accept `durationMinutes`, `synopsis`, `genres`, and `externalTrailerUrl`; responses expose both names. Conflicting values supplied through two aliases are rejected.

Movie requests ignore read-only IDs, timestamps, and nested detail sections when clients submit an existing movie response; these values cannot overwrite server-owned data. Movie updates require a nonblank title. Omitted fields preserve stored information; explicitly supplied null clears optional fields, and `[]` clears a collection. Core language/duration remain required on creation, duration must be positive, and operational status cannot be explicitly null. New movies can have no poster. Optional enhanced fields can be populated later. Budget requires a recognized ISO currency code. Production dates are checked for chronological consistency.

`UPCOMING`, `ACTIVE`, and `ENDED` retain their operational meaning. In particular, `ENDED` still blocks show creation. `releaseStatus` (`UPCOMING`, `RELEASED`, `DELAYED`, `CANCELLED`) and `productionStatus` (`ANNOUNCED`, `PRE_PRODUCTION`, `FILMING`, `POST_PRODUCTION`, `COMPLETED`) are separate and nullable. Legacy `ACTIVE` is not inferred to mean `RELEASED`.

`MovieMedia` and `MovieCredit` are authoritative. Legacy movie media, cast, and director fields are synchronized projections so booking notifications, ticket emails, and old clients can continue using them. Person renaming updates associated movies' cast/director projections. Legacy production text is retained and mapped to company names, never to a fabricated individual producer. With multiple production companies, the first is the legacy `production` projection; with multiple directors, the first credited director is the legacy `director` projection.

Legacy cast writes update the listed people, retain character details for matching names, and remove cast credits for removed names. Legacy poster-list writes replace the poster selection without deleting unrelated videos/stills. Legacy trailer/backdrop writes update the first corresponding record; blank/null removes that record, and additional versions remain available. Rich media and credit editors should use the dedicated endpoints. Do not send stale legacy cast/media fields when only updating basic information.

## Media types

Images: `POSTER`, `THEATRICAL_POSTER`, `FIRST_LOOK_POSTER`, `CHARACTER_POSTER`, `STILL`, `BEHIND_THE_SCENES_PHOTO`, `EVENT_PHOTO`, `OTHER_IMAGE`.

Videos: `MOTION_POSTER`, `TEASER`, `TRAILER`, `TRAILER_VERSION`, `BEHIND_THE_SCENES`, `MAKING_OF`, `INTERVIEW`, `EVENT_VIDEO`, `PROMOTIONAL_VIDEO`. `OTHER` supports other external media.

Every media record accepts `mediaType`, `mediaUrl`, optional `title` (the display label), `description`, `thumbnailUrl`, `language`, `sourcePlatform`, `publishedAt`, and flags `isOfficial`/`isPrimary`. `displayOrder` is optional: new entries append by default, updates preserve order when omitted. The original supplied URL is stored. URLs must be syntactically valid absolute HTTP/HTTPS URLs without embedded credentials, with a maximum length of 2000. The backend does not receive files, so the old file-count/file-size upload restrictions do not apply. Legacy poster arrays accept up to 100 entries per request.

`embedUrl` is derived for recognized YouTube video IDs and public Vimeo numeric URLs. Unsupported URLs remain usable as external links with `embedUrl: null`; the backend does not claim embed availability or contact the platform. Verified official social links require an explicit administrator assertion; supplying a URL does not verify a person or account.

Only poster types can be primary. The first poster is selected automatically when necessary. Primary changes lock the movie, flush the previous primary, and use a database unique constraint to prevent concurrent duplicate primaries. Reordering requires every media ID exactly once and rejects foreign or duplicate IDs.

### Frontend examples

Create a movie using the current fields or enhanced aliases:

```json
{
  "title": "Example Movie",
  "language": "Telugu",
  "languages": ["Telugu", "Hindi", "English"],
  "durationMinutes": 120,
  "synopsis": "An optional synopsis",
  "genres": ["Drama", "Thriller"],
  "status": "UPCOMING",
  "productionStatus": "POST_PRODUCTION"
}
```

Add a labeled video with `POST /api/v1/movies/{movieId}/media`:

```json
{
  "mediaType": "TEASER",
  "title": "Official Telugu Teaser",
  "mediaUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
  "thumbnailUrl": "https://example.com/teaser-thumb.jpg",
  "language": "Telugu",
  "isOfficial": true
}
```

Add a poster or still through the same route:

```json
{
  "mediaType": "THEATRICAL_POSTER",
  "title": "Release Poster",
  "mediaUrl": "https://example.com/poster.jpg",
  "isPrimary": true
}
```

Use `STILL`, `EVENT_PHOTO`, or `BEHIND_THE_SCENES_PHOTO` for other image labels. Use `TRAILER`, `MAKING_OF`, `INTERVIEW`, or `EVENT_VIDEO` for additional video labels. Render images from `mediaUrl`; embed only a nonnull `embedUrl`, otherwise offer an external link.

## Actors and technicians

Create a reusable person with `POST /api/v1/people`:

```json
{
  "fullName": "Example Professional",
  "biography": "Professional biography",
  "profileImageUrl": "https://example.com/profile.jpg",
  "skills": ["Acting", "Editing", "Cinematography"],
  "officialWebsite": "https://example.com"
}
```

Attach an actor with `POST /api/v1/movies/{movieId}/credits`:

```json
{
  "personId": "PERSON_UUID",
  "creditType": "CAST",
  "roleTitle": "Actor",
  "characterName": "Example Character",
  "characterDescription": "A movie-specific character description",
  "characterImageUrl": "https://example.com/character.jpg",
  "characterCategory": "HERO",
  "characterOccupation": "Teacher",
  "characterRelationships": {"friend": "Another Character"},
  "billingOrder": 0,
  "isMainCast": true
}
```

Attach a technician using the same person ID:

```json
{
  "personId": "PERSON_UUID",
  "creditType": "CREW",
  "department": "Post-production",
  "roleTitle": "Film editor",
  "billingOrder": 1
}
```

Skills, biography, name, profile image URL, and social links belong to the Person. Department/job title and actor character details belong to each movie credit. Crew roles are text values so new jobs need no schema change. The same person may hold distinct roles or portray distinct characters in one movie; equivalent type/department/role/character combinations cannot be duplicated. Shared people are never cascade-deleted when a movie or credit is removed. No global person-delete endpoint is provided.

## Endpoints

All paths start with `/api/v1`. GET catalog routes are public, matching existing behavior. Every catalog mutation requires `ROLE_ADMIN`. New child-resource POST endpoints return 201, DELETE returns 204; existing movie CRUD retains its status behavior. Error responses use the existing `ApiErrors` envelope and field-level Bean Validation errors.

| Method | Path | Purpose |
|---|---|---|
| GET | `/movies` | Paginated list; filters `title`, `genre`, `language`, `certification`, `status`, `releaseStatus`, `productionStatus` |
| GET | `/movies/{id}` | Details with media, ordered cast credits, and crew grouped by department |
| POST / PUT / DELETE | `/movies` / `/movies/{id}` | Existing movie management |
| GET / POST | `/movies/{movieId}/media` | List/add media |
| PUT / DELETE | `/movies/{movieId}/media/{mediaId}` | Update/remove media |
| PUT | `/movies/{movieId}/media/{mediaId}/primary` | Select primary poster |
| PUT | `/movies/{movieId}/media/order` | Reorder using `{"mediaIds":["ID_1","ID_2"]}` |
| GET / POST | `/movies/{movieId}/credits` | List/add credits |
| PUT / DELETE | `/movies/{movieId}/credits/{creditId}` | Update/remove credit |
| GET | `/movies/{movieId}/cast` | Cast in billing order (unassigned order last) |
| GET | `/movies/{movieId}/crew` | Crew grouped by department |
| GET / POST | `/people` | Paginated name search (`name`) / create person |
| GET / PUT | `/people/{personId}` | Read/update profile |
| GET / POST | `/people/{personId}/social-links` | Read/add social links |
| PUT / DELETE | `/people/{personId}/social-links/{linkId}` | Update/remove social link |
| GET | `/people/{personId}/filmography` | Paginated movie-specific credits |
| GET | `/people/{personId}/movies` | Paginated distinct movies for this person |

`page` defaults to 0, `size` to 20; invalid values return 400, and size is capped at 100. Movie lists exclude detailed media/credits; details assemble DTOs inside a transaction, batch person reads, and batch collection loading rather than serializing JPA relationships. There is no existing movie slug contract, so no slug route is introduced.

Person PUT and media/credit/social-link PUT replace those resource fields; submit the complete editable object. Movie PUT uses the compatibility-preserving omission behavior described above.

## Database rollout

No production database has been modified by this implementation. All migrations must be tested against a restored deployment backup before starting the upgraded application.

1. Back up the database and verify the current catalog schema matches V1 (including `movie_genres`, `movie_cast`, and `movie_posters`). Inspect any existing `flyway_schema_history`; the supplied baseline assumes no previously applied versioned migrations, as in this repository. Resolve differing migration history/schema in the deployment copy before rollout.
2. Profiles now enable Flyway, `baseline-on-migrate: true`, and baseline version 1. On an existing nonempty database without history, Flyway records V1 as the baseline, then applies V2/V3. On an empty database, V1 creates the legacy catalog tables before the enhancements. Flyway startup runs before Hibernate.
3. V2 adds optional movie columns, language/location/company collections, people/skills/social links, media, credits, indexes, foreign keys, and primary-poster/duplicate-credit constraints. It makes the old required `poster_url` nullable for incomplete profiles. It does not drop legacy columns.
4. V3 backfills language/company text, valid posters/trailers/backdrops, cast credits, and director credits. IDs are deterministic, and existence checks plus Flyway history prevent duplicate imports. Existing movies, statuses, genre rows, cast rows, shows, bookings, and reviews remain intact. Genres were already normalized collection rows; existing values are preserved and new writes trim/deduplicate them.
5. People imported from legacy names are scoped to each movie, avoiding unsafe global identity merges. Cast/director names are treated as existing names; commas are not blindly split because the current database already stores individual rows and a comma can belong to a person's name.
6. Unsupported legacy poster/video values are preserved in their original columns and in `catalog_legacy_issues` with the full original value. They are excluded from new URL-only response projections. Replace them using the media APIs; media writes then update legacy projections. Existing booking/email code can still access retained legacy poster storage until remediation. Review all issues before deployment sign-off.
7. Existing profiles keep Hibernate `ddl-auto=update` because unrelated application tables are not yet under versioned migrations. This catalog change does not pretend the entire application schema has been converted to Flyway. A future whole-schema baseline can enable `validate`; do not switch it blindly while unrelated tables are still Hibernate-managed.
8. Deploy frontend URL-input forms and dedicated people/credit/media editors. Existing clients need no immediate route changes; old file/Base64 workflows must stop submitting embedded images.

MySQL DDL is not fully transactional. If a migration fails, restore the deployment copy or reconcile the partial schema before using Flyway repair; do not simply rerun partially applied V2 or delete migration history. Do not edit applied migration files—use a new version.

Optional soundtrack, distribution, awards, box-office, and related-movie modules remain outside this core implementation, as planned. No financial figures are fabricated.

## Verification

Run `mvn test` and `mvn package`. Tests use an isolated H2 database in MySQL mode for API/service integration, authorization, legacy compatibility, constraints, and backfill. H2 passing does not prove deployment-specific MySQL schema/collation compatibility.
