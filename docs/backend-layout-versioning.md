# Backend layout persistence

`layout_versions` and `layout_seats` define show seat maps. Legacy `seats` rows
are retained as recovery evidence, not used as a booking fallback. Screen reads
project the active version; show availability projects the captured version.

## Entity-driven schema evolution

No SQL migration or manual database change is required by this change. The
existing profiles use Hibernate `ddl-auto: update`. At application deployment,
entity mappings add:

- `screens.active_layout_version_id`: authoritative active version reference.
- `layout_versions.active_screen_id`: unique nullable active-screen slot.
  Entity lifecycle callbacks populate it only for ACTIVE versions.
- `layout_seats.seat_id`: stable booking-facing seat identity, unique per version.
- `layout_recoveries`: provenance of explicitly requested legacy repairs.
- Legacy `seats.label` capacity is aligned with the versioned 80-character label.

Existing primary keys and legacy seat/booking/hold records remain intact. For an
old versioned seat with NULL `seat_id`, its existing primary key remains its
booking identity. Copies preserve that identity in `seat_id`, while allocating a
new internal row primary key. The public JSON `id` is the booking identity.
Deployments that use schema validation instead of update need an approved schema
provisioning mechanism before using these mappings. No production deployment or
reconciliation is performed by tests.

## Layout lifecycle

Creating a screen validates seats and creates/publishes the initial complete
layout in one transaction. Dimensions-only requests (missing or empty `seats`)
generate a REGULAR, enabled grid from positive `rows` and `seatsPerRow`, with
labels A1, A2, ..., Z1, AA1, etc. Generated grids are limited to 10000 seats.
Explicit seat arrays retain their custom arrangement; dimensions do not fill gaps. Draft creation defaults to the active layout. Saving
seats replaces the draft using an identity-preserving diff; return unchanged
seats' public IDs, and omit IDs for newly added seats. A seat without an ID can
match an existing seat by label and position. Unknown IDs are rejected by the
versioned endpoint. Legacy screen PUT translates known IDs and allocates IDs for
new seats, then creates/saves/publishes a new version atomically. It never rewrites
the legacy seat collection or a published seat map.

Screen locks serialize version allocation, publishing, and show creation.
Publishing archives the previous active version and updates the active reference
atomically. Versions referenced by shows cannot be edited or published, including
incorrectly marked drafts. Existing shows keep their version when new layouts
are published. Screens with shows cannot be deleted through cascading deletion.

## Show availability contract

`GET /api/v1/shows/{id}/availability` returns every seat in `layout.seats`, with
public `id`, `layoutVersionId`, label, row/column, category/color, and:

| state | disabled | clickable | holdExpiresAt |
| --- | --- | --- | --- |
| AVAILABLE | false | true | null |
| RESERVED | true | false | null |
| HELD | true | false | active hold expiry |
| DISABLED | true | false | null |

These `disabled` values are response-only occupancy overlays. Stored geometry,
numbering, category and disabled flags never change during booking, holds or
cancellation. Persistent disabled flags take precedence, then PENDING/CONFIRMED
bookings, then active unexpired holds. Overlapping reservations/holds are counted
once. Cancelled bookings and expired holds stop blocking seats.

The response includes `reservedSeatIds`, `heldSeatIds`, and `availableSeatCount`.
For compatibility, `bookedSeatIds` includes all unclickable seats, and
`screen.seats` uses the same show-specific geometry and unclickable flags.
Clients should use `layout.seats.state` to distinguish reserved, held and disabled
seats. Held seats remain unclickable in availability, but their owner may convert
the hold into a booking or refresh it via the hold endpoint. Booking and hold
validation are serialized per show and reject foreign/disabled seats and other
users' active holds. All available counts use this same occupancy resolver.

## Explicit legacy reconciliation

Admin-only endpoints:

- `GET /api/v1/theatres/screens/{screenId}/layout-compatibility`
- `POST /api/v1/theatres/screens/{screenId}/layout-compatibility/reconcile`

GET assesses the screen's active map and every show without modifying data.
POST plans and validates all repairs before writing, locks the screen and its
shows, and creates recovered versions with audit records. Existing versions,
seats, booking seat IDs, and hold seat IDs are retained. Only explicit recovery
may retarget an inconsistent show; healthy shows are untouched. Repeating a
successful reconciliation makes no further recovered versions.

A missing show reference can be recovered from its unique screen/numeric version
when that version has reliable seats or a snapshot.

Recovery prefers versioned seats, or the saved snapshot if the map is missing,
partial, or inconsistent. Legacy IDs may be mapped only when label, position,
category, disabled flag and color match exactly. All historical booking and hold
IDs must resolve; otherwise the transaction is rejected. A healthy older map is
never replaced merely because the current screen has more or different seats.

When historical geometry is absent, POST rejects by default. The optional
`adoptCurrentLegacyLayout=true` parameter explicitly accepts current legacy
geometry, and is permitted for a show only when its numeric version matches the
screen's. Use the GET endpoint with the same parameter to review that proposal
first. Duplicate active versions without an authoritative reference require
explicit adoption or further data investigation.

Availability/booking do not perform repairs. Missing maps, snapshot geometry
mismatches, and orphaned active occupancy return
`409 LAYOUT_RECONCILIATION_REQUIRED` instead of displaying a different map or
making an occupied physical seat available.

## Validation

`LayoutIntegrationTest` uses isolated H2 in MySQL mode, with Flyway disabled and
entity-generated tables. `LayoutEntityUpgradeTest` additionally verifies repeated
Hibernate schema update from the previous entity shape without SQL scripts.
The integration tests cover initialization, validation and rollback,
stable IDs and swaps, legacy screen saves, publishing and database uniqueness,
concurrent publishing and booking, disabled/foreign seats and holds, partial/full
user and admin cancellation, expiry, missing/partial legacy maps and preservation
of booking IDs during recovery. The existing backend suite runs separately from
any application database. Production MySQL upgrade execution is not part of
these tests.
