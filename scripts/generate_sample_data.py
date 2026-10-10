#!/usr/bin/env python3
"""Generate deterministic MySQL fixtures. Run after Hibernate has created the schema."""

import argparse
import json
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
p = argparse.ArgumentParser()
p.add_argument("--count", type=int, default=100)
p.add_argument("--admin-email", default="admin@example.test")
a = p.parse_args()
if a.count < 100:
    p.error("--count must be at least 100")


def uid(kind, i):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, f"av-sample/{kind}/{i}"))


class Expr(str):
    pass


def sql(v):
    if isinstance(v, Expr):
        return v
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "1" if v else "0"
    if isinstance(v, (int, float)):
        return str(v)
    return "'" + str(v).replace("\\", "\\\\").replace("'", "''") + "'"


statements = [
    "-- Generated local/test fixtures; import only into a development database.",
    "-- Re-running fails on duplicate fixture IDs and rolls back; no existing data is deleted.",
    "SET NAMES utf8mb4;",
    "START TRANSACTION;",
]
counts = {}
now = Expr("UTC_TIMESTAMP(6)")
future = Expr("DATE_ADD(CURDATE(), INTERVAL 30 DAY)")


def insert(table, **row):
    statements.append(
        f"INSERT INTO `{table}` ("
        + ",".join(f"`{k}`" for k in row)
        + ") VALUES ("
        + ",".join(sql(v) for v in row.values())
        + ");"
    )
    counts[table] = counts.get(table, 0) + 1


def timed(table, **row):
    insert(table, **row, created_at=now, updated_at=now)


# The service hashes emails into 64 lock buckets. Extra IDs are inert fixtures.
for i in range(a.count):
    statements.append(f"INSERT IGNORE INTO auth_locks (id) VALUES ({i});")
counts["auth_locks"] = a.count
for i in range(1, a.count + 1):
    u, m, t, s, v, b = [
        uid(k, i) for k in ("user", "movie", "theatre", "screen", "layout", "booking")
    ]
    email = a.admin_email if i == 1 else f"user{i:03}@example.test"
    mobile = f"+919000{i:06}"
    timed(
        "users",
        id=u,
        name=f"Sample Viewer {i:03}",
        mobile=mobile,
        email=email,
        role="ADMIN" if i == 1 else "USER",
        enabled=True,
    )
    # Deliberately expired, unusable BCrypt-shaped history; never seeds a usable OTP.
    insert(
        "otp_verifications",
        mobile=email,
        channel="EMAIL",
        otp_hash="$2a$10$" + "x" * 53,
        expires_at=Expr("DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 2 HOUR)"),
        attempt_count=0,
        verified=False,
        created_at=Expr("DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 3 HOUR)"),
    )
    timed(
        "movies",
        id=m,
        title=f"Sample Movie {i:03}",
        poster_url=f"https://example.test/posters/{i}.jpg",
        backdrop_url=None,
        trailer_url=None,
        description=f"Fictional test movie {i}",
        language=["Telugu", "Hindi", "English"][i % 3],
        duration=120,
        release_date=Expr("CURDATE()"),
        certification="U/A",
        director=f"Director {i}",
        production="Sample Studios",
        status="ACTIVE",
    )
    insert(
        "movie_posters",
        movie_id=m,
        poster_order=0,
        poster_url=f"https://example.test/posters/{i}.jpg",
    )
    insert("movie_genres", movie_id=m, genre=["Drama", "Comedy", "Action"][i % 3])
    insert("movie_cast", movie_id=m, cast_name=f"Actor {i}")
    timed(
        "theatres",
        id=t,
        name=f"Sample Theatre {i:03}",
        address=f"{i} Cinema Road",
        city=["Hyderabad", "Bengaluru", "Chennai"][i % 3],
        state="Sample State",
        contact=mobile,
        status="ACTIVE",
        map_url=None,
    )
    insert(
        "venue_media",
        id=uid("media", i),
        theatre_id=t,
        type="IMAGE",
        url=f"https://example.test/venues/{i}.jpg",
        title="Front entrance",
    )
    timed(
        "screens",
        id=s,
        theatre_id=t,
        name="Screen 1",
        number=1,
        row_count=1,
        seats_per_row=4,
        layout_version=1,
        layout_locked=True,
        status="ACTIVE",
    )
    seat_rows = []
    for j in range(1, 5):
        # Shared identifiers intentionally let both legacy hold and version booking APIs work.
        sid = uid("seat", i * 10 + j)
        insert(
            "seats",
            id=sid,
            screen_id=s,
            label=f"A{j}",
            category="REGULAR",
            color="#8B7A91",
            disabled=False,
            row_number=1,
            column_number=j,
        )
        seat_rows.append(
            dict(
                id=sid,
                layoutVersionId=v,
                label=f"A{j}",
                rowNumber=1,
                columnNumber=j,
                category="REGULAR",
                disabled=False,
                color="#8B7A91",
            )
        )
    insert(
        "layout_versions",
        id=v,
        screen_id=s,
        version_number=1,
        name="Layout V1",
        status="ACTIVE",
        published_at=now,
        snapshot=json.dumps(dict(seats=seat_rows, rows=1, seatsPerRow=4)),
        created_at=now,
    )
    for row in seat_rows:
        insert(
            "layout_seats",
            id=row["id"],
            layout_version_id=v,
            label=row["label"],
            row_number=1,
            column_number=row["columnNumber"],
            category="REGULAR",
            disabled=False,
            color="#8B7A91",
        )
    timed(
        "shows",
        id=uid("show", i),
        movie_id=m,
        theatre_id=t,
        screen_id=s,
        date=future,
        start_time="18:00:00",
        end_time="20:00:00",
        ticket_type="PAID",
        ticket_price=200,
        max_tickets_per_mobile=6,
        hold_minutes=5,
        layout_version=1,
        layout_version_id=v,
        seat_prices="{}",
        require_admin_confirmation=True,
        status="OPEN",
    )
    timed(
        "bookings",
        id=b,
        user_id=u,
        show_id=uid("show", i),
        ticket_count=4,
        total_amount=800,
        status="CONFIRMED",
        confirmation_status="CONFIRMED",
        attended=False,
        movie=f"Sample Movie {i:03}",
        theatre=f"Sample Theatre {i:03}",
        screen="Screen 1",
        date=Expr("CAST(DATE_ADD(CURDATE(), INTERVAL 30 DAY) AS CHAR)"),
        time="18:00",
        mobile=mobile,
        email=email,
        ticket_code=f"AV-SAMPLE-{i:06}",
    )
    for row in seat_rows:
        insert("booking_seats", booking_id=b, seat_id=row["id"])
    timed(
        "payments",
        id=uid("payment", i),
        booking_id=b,
        amount=800,
        status="VERIFIED",
        provider="TEST",
        provider_reference=f"SAMPLE-{i:06}",
    )
    timed(
        "seat_holds",
        id=uid("hold", i),
        show_id=uid("show", i),
        seat_id=seat_rows[0]["id"],
        user_id=u,
        held_at=Expr("DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 1 HOUR)"),
        expires_at=Expr("DATE_SUB(UTC_TIMESTAMP(6), INTERVAL 50 MINUTE)"),
        status="EXPIRED",
    )
    timed(
        "waiting_list_entries",
        id=uid("waiting", i),
        show_id=uid("show", i),
        user_id=uid("user", 2 if i == a.count else max(2, i + 1)),
        requested_seats_count=1,
        position=1,
        status="WAITING",
    )
    timed(
        "reviews",
        id=uid("review", i),
        movie_id=m,
        user_id=u,
        author=f"Sample Viewer {i:03}",
        title="Enjoyable screening",
        text="Fictional sample review for API testing.",
        rating=i % 5 + 1,
        is_highlighted=i % 10 == 0,
        status="APPROVED",
    )
    insert(
        "notifications",
        id=uid("notification", i),
        user_id=u,
        type="BOOKING_CREATED",
        message=f"Sample booking {i} confirmed.",
        created_at=now,
    )
statements.append("COMMIT;")
statements.append(
    "SELECT "
    + ", ".join(f"(SELECT COUNT(*) FROM `{t}`) AS `{t}`" for t in counts)
    + ";"
)
(ROOT / "sample-data" / "seed.sql").write_text("\n".join(statements) + "\n")
(ROOT / "sample-data" / "manifest.json").write_text(
    json.dumps(
        dict(
            counts=counts,
            adminEmail=a.admin_email,
            userEmail="user002@example.test",
            userId=uid("user", 2),
            targetUserId=uid("user", 3),
            waitingShowId=uid("show", a.count),
            waitingBookingId=uid("booking", a.count),
            seedBookingId=uid("booking", 2),
        ),
        indent=2,
    )
    + "\n"
)
print(json.dumps(counts, indent=2))
