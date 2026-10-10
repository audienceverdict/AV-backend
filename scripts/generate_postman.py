#!/usr/bin/env python3
"""Generate the complete, ordered Postman workflow and endpoint index."""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / "sample-data/manifest.json").read_text())
folders = []
index = []


def folder(name):
    f = {"name": name, "item": []}
    folders.append(f)
    return f["item"]


def add(
    items,
    name,
    method,
    path,
    body=None,
    auth="adminToken",
    capture=None,
    script="",
    desc="",
    codes=(200,),
):
    req = {
        "method": method,
        "header": [],
        "url": "{{baseUrl}}/api/v1" + path,
        "description": desc,
    }
    req["auth"] = (
        {"type": "noauth"}
        if auth is None
        else {
            "type": "bearer",
            "bearer": [{"key": "token", "value": "{{" + auth + "}}", "type": "string"}],
        }
    )
    if body is not None:
        req["header"] = [{"key": "Content-Type", "value": "application/json"}]
        req["body"] = {
            "mode": "raw",
            "raw": json.dumps(body, indent=2),
            "options": {"raw": {"language": "json"}},
        }
    js = (
        'pm.test("Expected HTTP status", () => pm.expect(pm.response.code).to.be.oneOf('
        + json.dumps(list(codes))
        + "));"
    )
    if capture:
        js += (
            "\nif (pm.response.code < 300) pm.environment.set("
            + json.dumps(capture)
            + ", pm.response.json().id);"
        )
    js += "\n" + script
    items.append(
        {
            "name": name,
            "request": req,
            "event": [
                {
                    "listen": "test",
                    "script": {"type": "text/javascript", "exec": js.splitlines()},
                }
            ],
        }
    )
    index.append(
        (
            len(index) + 1,
            name,
            method,
            "{{baseUrl}}/api/v1" + path,
            auth or "Public",
            desc,
        )
    )


a = folder("01 New user: request OTP, verify, register, bearer token")
token = 'if (pm.response.code === 200) { const r=pm.response.json(); if(r.accessToken) pm.environment.set("userToken",r.accessToken); if(r.user) pm.environment.set("userId",r.user.id); }'
add(
    a,
    "Request new user email OTP",
    "POST",
    "/auth/email-otp/request",
    {"email": "{{registrationEmail}}"},
    None,
    desc="Enter a deliverable unused email. Read the code from your inbox or local dev log; set userOtp before verification.",
)
add(
    a,
    "Verify new user OTP",
    "POST",
    "/auth/email-otp/verify",
    {"email": "{{registrationEmail}}", "otp": "{{userOtp}}"},
    None,
    script=token,
)
add(
    a,
    "Register verified new profile and save bearer token",
    "POST",
    "/auth/email-otp/register",
    {
        "email": "{{registrationEmail}}",
        "name": "Postman Viewer",
        "mobile": "{{registrationMobile}}",
    },
    None,
    script=token,
    desc="Only needed when verify returned registrationRequired=true. Must run before OTP expires.",
)
add(a, "Get current profile", "GET", "/auth/me", auth="userToken")
add(
    a,
    "Update current profile",
    "PUT",
    "/auth/me",
    {"name": "Postman Viewer Updated", "email": "{{registrationEmail}}"},
    "userToken",
)
a = folder("02 Admin OTP login")
add(
    a,
    "Request configured admin OTP",
    "POST",
    "/auth/email-otp/request",
    {"email": "{{adminEmail}}"},
    None,
    desc="adminEmail must equal app.admin.email and an enabled ADMIN account must exist. Set adminOtp from inbox/log.",
)
add(
    a,
    "Verify admin OTP and save admin bearer token",
    "POST",
    "/auth/email-otp/verify",
    {"email": "{{adminEmail}}", "otp": "{{adminOtp}}"},
    None,
    script='if(pm.response.code===200 && pm.response.json().accessToken) pm.environment.set("adminToken",pm.response.json().accessToken);',
)
a = folder("03 Existing user OTP login (optional alternative to 01)")
add(
    a,
    "Request existing user OTP",
    "POST",
    "/auth/email-otp/request",
    {"email": "{{existingUserEmail}}"},
    None,
    desc="Optional: use instead of folder 01. Seed user uses example.test; use local dev logging or change fixture email to an inbox you control.",
)
add(
    a,
    "Verify existing user and save user bearer token",
    "POST",
    "/auth/email-otp/verify",
    {"email": "{{existingUserEmail}}", "otp": "{{existingUserOtp}}"},
    None,
    script=token,
)
a = folder("04 Admin user management")
for name, path in [
    ("List users", "?page=0&size=100"),
    ("Get sample user", "/{{targetUserId}}"),
]:
    add(a, name, "GET", "/admin/users" + path)
for enabled in (False, True):
    add(
        a,
        ("Disable" if not enabled else "Enable") + " sample user",
        "PATCH",
        "/admin/users/{{targetUserId}}/status",
        {"enabled": enabled},
    )
for role in ("ADMIN", "USER"):
    add(
        a,
        "Set sample user role " + role,
        "PATCH",
        "/admin/users/{{targetUserId}}/role",
        {"role": role},
        desc="Targets sample user 003, not either active login account.",
    )
a = folder("05 Movie master data")
movie = {
    "title": "Postman Movie",
    "posterUrl": "https://example.test/poster.jpg",
    "posterImages": ["https://example.test/poster.jpg"],
    "genre": ["Drama"],
    "language": "Telugu",
    "duration": 120,
    "releaseDate": "{{showDate}}",
    "description": "API test movie",
    "cast": ["Sample Actor"],
    "status": "ACTIVE",
}
add(a, "Create movie", "POST", "/movies", movie, capture="movieId")
add(
    a,
    "List active movies with pagination",
    "GET",
    "/movies?status=ACTIVE&page=0&size=100",
    auth=None,
)
add(a, "Get movie", "GET", "/movies/{{movieId}}", auth=None)
add(
    a,
    "Update movie",
    "PUT",
    "/movies/{{movieId}}",
    dict(movie, title="Postman Movie Updated"),
)
a = folder("05b Movie media, people, and credits (URL-only)")
add(a, "Search enhanced movie catalog", "GET", "/movies?title=Postman&genre=Drama&language=Telugu&page=0&size=20", auth=None)
add(a, "Create reusable actor and technician", "POST", "/people", {
    "fullName": "Postman Professional", "biography": "Actor and film editor",
    "profileImageUrl": "https://example.test/person.jpg", "skills": ["Acting", "Editing"]
}, capture="catalogPersonId", codes=(201,))
add(a, "Search people", "GET", "/people?name=Postman&page=0&size=20", auth=None)
add(a, "Get person profile", "GET", "/people/{{catalogPersonId}}", auth=None)
add(a, "Update technician profile", "PUT", "/people/{{catalogPersonId}}", {
    "fullName": "Postman Professional", "biography": "Actor, editor, and cinematographer",
    "profileImageUrl": "https://example.test/person.jpg", "skills": ["Acting", "Editing", "Cinematography"]
})
add(a, "Add social profile", "POST", "/people/{{catalogPersonId}}/social-links", {
    "platform": "INSTAGRAM", "profileUrl": "https://www.instagram.com/example", "isVerifiedOfficial": False
}, capture="catalogSocialId", codes=(201,))
add(a, "List social profiles", "GET", "/people/{{catalogPersonId}}/social-links", auth=None)
add(a, "Update social profile", "PUT", "/people/{{catalogPersonId}}/social-links/{{catalogSocialId}}", {
    "platform": "INSTAGRAM", "profileUrl": "https://www.instagram.com/example", "username": "example", "isVerifiedOfficial": False
})
actor_credit = {
    "personId": "{{catalogPersonId}}", "creditType": "CAST", "roleTitle": "Actor",
    "characterName": "Lead Character", "characterCategory": "HERO",
    "characterImageUrl": "https://example.test/character.jpg", "isMainCast": True, "billingOrder": 0
}
add(a, "Add actor and character", "POST", "/movies/{{movieId}}/credits", actor_credit, capture="catalogCreditId", codes=(201,))
add(a, "Update actor character", "PUT", "/movies/{{movieId}}/credits/{{catalogCreditId}}", dict(actor_credit, characterDescription="Movie-specific character profile"))
for role in ("Film editor", "VFX supervisor"):
    add(a, "Add crew role " + role, "POST", "/movies/{{movieId}}/credits", {
        "personId": "{{catalogPersonId}}", "creditType": "CREW", "department": "Post-production", "roleTitle": role
    }, codes=(201,))
for suffix in ("credits", "cast", "crew"):
    add(a, "Get movie " + suffix, "GET", "/movies/{{movieId}}/" + suffix, auth=None)
add(a, "Get person filmography", "GET", "/people/{{catalogPersonId}}/filmography?page=0&size=20", auth=None)
add(a, "Get distinct credited movies", "GET", "/people/{{catalogPersonId}}/movies?page=0&size=20", auth=None)
add(a, "Add primary theatrical poster", "POST", "/movies/{{movieId}}/media", {
    "mediaType": "THEATRICAL_POSTER", "title": "Release poster", "mediaUrl": "https://example.test/release.jpg", "isPrimary": True
}, capture="catalogPosterId", codes=(201,))
for media_type in ("TRAILER", "TEASER", "MAKING_OF", "INTERVIEW", "EVENT_VIDEO"):
    add(a, "Add labeled video " + media_type, "POST", "/movies/{{movieId}}/media", {
        "mediaType": media_type, "title": media_type.replace("_", " ").title(),
        "mediaUrl": "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "language": "Telugu", "isOfficial": True
    }, codes=(201,))
add(a, "Add labeled movie still", "POST", "/movies/{{movieId}}/media", {
    "mediaType": "STILL", "title": "Movie still", "mediaUrl": "https://example.test/still.jpg"
}, capture="catalogMediaId", codes=(201,))
add(a, "Update media label", "PUT", "/movies/{{movieId}}/media/{{catalogMediaId}}", {
    "mediaType": "STILL", "title": "Updated movie still", "mediaUrl": "https://example.test/still.jpg"
})
add(a, "Select primary poster", "PUT", "/movies/{{movieId}}/media/{{catalogPosterId}}/primary")
add(a, "List movie media and capture order", "GET", "/movies/{{movieId}}/media", auth=None,
    script='if(pm.response.code===200) pm.environment.set("catalogMediaIds", JSON.stringify(pm.response.json().map(x=>x.id).reverse()));')
add(a, "Reorder movie media", "PUT", "/movies/{{movieId}}/media/order", {"mediaIds": []})
a[-1]["request"]["body"]["raw"] = '{"mediaIds": {{catalogMediaIds}}}'
add(a, "Delete sample movie still", "DELETE", "/movies/{{movieId}}/media/{{catalogMediaId}}", codes=(204,))
add(a, "Delete sample cast credit", "DELETE", "/movies/{{movieId}}/credits/{{catalogCreditId}}", codes=(204,))
add(a, "Delete sample social link", "DELETE", "/people/{{catalogPersonId}}/social-links/{{catalogSocialId}}", codes=(204,))
a = folder("06 Theatre, screen, layout master data")
theatre = {
    "name": "Postman Theatre",
    "address": "1 Cinema Road",
    "city": "Hyderabad",
    "state": "Telangana",
    "contact": "+919999999999",
    "status": "ACTIVE",
    "media": [
        {
            "type": "IMAGE",
            "url": "https://example.test/theatre.jpg",
            "title": "Entrance",
        }
    ],
}
seat = [
    {
        "label": "A" + str(i),
        "rowNumber": 1,
        "columnNumber": i,
        "category": "REGULAR",
        "disabled": False,
        "color": "#8B7A91",
    }
    for i in range(1, 5)
]
screen = {
    "name": "Screen 1",
    "number": 1,
    "rows": 1,
    "seatsPerRow": 4,
    "status": "ACTIVE",
    "seats": seat,
}
add(a, "Create theatre", "POST", "/theatres", theatre, capture="theatreId")
add(a, "List theatres", "GET", "/theatres?page=0&size=100", auth=None)
add(a, "Get theatre", "GET", "/theatres/{{theatreId}}", auth=None)
add(a, "Update theatre", "PUT", "/theatres/{{theatreId}}", theatre)
add(
    a,
    "Create screen with seats",
    "POST",
    "/theatres/{{theatreId}}/screens",
    screen,
    capture="screenId",
)
seat = [dict(x, label="B" + str(i)) for i, x in enumerate(seat, 1)]
add(
    a,
    "Update screen before it is locked",
    "PUT",
    "/theatres/screens/{{screenId}}",
    dict(screen, seats=seat),
    desc="Run once before shows; this endpoint locks the screen. Replacement labels use row B to avoid conflicting with orphaned row A seats during persistence.",
)
add(
    a,
    "List screens and capture legacy seat IDs",
    "GET",
    "/theatres/{{theatreId}}/screens",
    auth=None,
    script='if(pm.response.code===200){const s=pm.response.json().find(x=>x.id===pm.environment.get("screenId")); if(s){pm.environment.set("seatId",s.seats[0].id);pm.environment.set("secondSeatId",s.seats[1].id);}}',
)
add(
    a,
    "List layout versions",
    "GET",
    "/theatres/screens/{{screenId}}/layout-versions",
    auth=None,
)
add(
    a,
    "Create draft layout",
    "POST",
    "/theatres/screens/{{screenId}}/layout-versions?name=Postman%20Layout",
    capture="layoutVersionId",
)
add(
    a,
    "Replace draft layout seats",
    "PUT",
    "/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/seats",
    seat,
)
add(
    a,
    "Get layout details",
    "GET",
    "/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}",
    auth=None,
)
add(
    a,
    "Publish layout",
    "POST",
    "/theatres/screens/{{screenId}}/layout-versions/{{layoutVersionId}}/publish",
)
a = folder("07 Show scheduling")
show = {
    "movieId": "{{movieId}}",
    "theatreId": "{{theatreId}}",
    "screenId": "{{screenId}}",
    "date": "{{showDate}}",
    "startTime": "18:00:00",
    "endTime": "20:00:00",
    "ticketType": "PAID",
    "ticketPrice": 200,
    "maxTicketsPerMobile": 6,
    "holdMinutes": 5,
    "requireAdminConfirmation": True,
    "status": "OPEN",
    "seatPrices": {"REGULAR": 200},
}
add(a, "Create show", "POST", "/shows", show, capture="showId")
add(
    a,
    "List shows with filters",
    "GET",
    "/shows?movieId={{movieId}}&theatreId={{theatreId}}&date={{showDate}}",
    auth=None,
)
add(a, "Get show", "GET", "/shows/{{showId}}", auth=None)
add(a, "Update show", "PUT", "/shows/{{showId}}", show)
add(
    a,
    "Get availability",
    "GET",
    "/shows/{{showId}}/availability",
    auth=None,
    desc="Version seat IDs differ from legacy screen IDs. This workflow books legacy IDs because seat-holds currently validate legacy IDs.",
)
a = folder("08 Seat holds, booking, payment, attendance")
add(
    a,
    "Hold seat",
    "POST",
    "/bookings/seat-holds",
    {"showId": "{{showId}}", "seatIds": ["{{seatId}}"]},
    "userToken",
)
add(
    a,
    "Get active holds",
    "GET",
    "/bookings/seat-holds?showId={{showId}}",
    auth="userToken",
)
booking = {"showId": "{{showId}}", "seatIds": ["{{seatId}}"], "termsAccepted": True}
add(
    a,
    "Create paid booking",
    "POST",
    "/bookings",
    booking,
    "userToken",
    capture="bookingId",
)
add(a, "Get my bookings", "GET", "/bookings/me", auth="userToken")
add(a, "Get my booking", "GET", "/bookings/{{bookingId}}", auth="userToken")
add(
    a, "Get booking payment", "GET", "/payments/booking/{{bookingId}}", auth="userToken"
)
add(
    a,
    "Verify payment",
    "POST",
    "/payments/booking/{{bookingId}}/verify",
    {"provider": "TEST", "providerReference": "POSTMAN-{{$guid}}"},
    "userToken",
    desc="Current backend marks payment VERIFIED from these fields; it has no external gateway callback endpoint.",
)
add(a, "Admin list bookings", "GET", "/bookings/admin")
add(a, "Admin confirm booking", "POST", "/bookings/admin/{{bookingId}}/confirm")
add(
    a, "Mark attended", "POST", "/bookings/admin/{{bookingId}}/attendance?attended=true"
)
a = folder("09 Reviews and notifications")
review = {
    "movieId": "{{movieId}}",
    "title": "Great film",
    "text": "Postman test review",
    "rating": 4,
}
add(a, "Create review", "POST", "/reviews", review, "userToken", capture="reviewId")
add(
    a,
    "Update my review",
    "PUT",
    "/reviews/{{reviewId}}",
    dict(review, rating=5),
    "userToken",
)
add(
    a,
    "Moderate and highlight review",
    "POST",
    "/reviews/admin/{{reviewId}}/moderate?status=APPROVED&highlighted=true",
)
add(
    a,
    "List approved reviews",
    "GET",
    "/reviews?movieId={{movieId}}&status=APPROVED",
    auth=None,
)
add(
    a,
    "Send show-booker campaign",
    "POST",
    "/notifications/admin/campaigns",
    {
        "target": "SHOW_BOOKERS",
        "subject": "Postman screening update",
        "message": "Hi {{name}}, your screening is scheduled.",
        "showId": "{{showId}}",
        "movieId": "{{movieId}}",
        "status": "CONFIRMED",
        "sortBy": "name",
        "sortDirection": "asc",
    },
    desc="Sends email to the account used for this test. Other supported targets: ALL_USERS and MOVIE_BOOKERS.",
)
add(
    a,
    "Send show reminder",
    "POST",
    "/notifications/admin/shows/{{showId}}/reminder",
    desc="Sends reminder email to show bookers.",
)
add(a, "Get my notifications", "GET", "/notifications", auth="userToken")
a = folder("10 Waiting list: sold out, release, offer, claim")
add(
    a,
    "Join sold-out seeded show waiting list",
    "POST",
    "/waiting-list",
    {"showId": "{{waitingShowId}}", "requestedSeatsCount": 1},
    "userToken",
    capture="waitingEntryId",
    desc="Use newly registered user from folder 01. user002 already has a seeded waiting entry for this show.",
)
add(a, "List my waiting entries", "GET", "/waiting-list", auth="userToken")
add(
    a,
    "Admin list waiting entries",
    "GET",
    "/waiting-list/admin",
    script='if(pm.response.code===200){const e=pm.response.json().find(x=>x.showId===pm.environment.get("waitingShowId")&&x.status==="WAITING"&&x.userId!==pm.environment.get("userId"));if(e)pm.environment.set("earlierWaitingEntryId",e.id);}',
)
add(
    a,
    "Release seeded booking seats to offer earlier entry",
    "POST",
    "/bookings/admin/{{waitingBookingId}}/cancel",
    {"reason": "Postman waiting list test"},
    desc="First release offers the seeded entry ahead of you. Next cancellation triggers your offer. Seed booking is changed by this scenario.",
)
add(
    a,
    "Create second booking for offer trigger",
    "POST",
    "/bookings",
    {
        "showId": "{{waitingShowId}}",
        "seatIds": ["{{waitingSeatId}}"],
        "termsAccepted": True,
    },
    "userToken",
    capture="offerTriggerBookingId",
)
add(
    a,
    "Cancel trigger booking to offer my entry",
    "POST",
    "/bookings/{{offerTriggerBookingId}}/cancel",
    auth="userToken",
)
add(
    a,
    "Claim waiting list offer",
    "POST",
    "/waiting-list/{{waitingEntryId}}/claim",
    auth="userToken",
    desc="Run within five minutes of cancellation. Claim marks CONVERTED; make a separate booking to reserve seats.",
)
add(
    a,
    "Cancel my waiting entry",
    "DELETE",
    "/waiting-list/{{waitingEntryId}}",
    auth="userToken",
)
a = folder("11 Cancellation and cleanup of Postman-created data")
add(
    a,
    "Cancel my booking",
    "POST",
    "/bookings/{{bookingId}}/cancel",
    {"seatIds": ["{{seatId}}"]},
    "userToken",
)
add(
    a,
    "Create second booking for admin cancellation",
    "POST",
    "/bookings",
    dict(booking, seatIds=["{{secondSeatId}}"]),
    "userToken",
    capture="adminCancelBookingId",
)
add(
    a,
    "Admin cancel booking with reason",
    "POST",
    "/bookings/admin/{{adminCancelBookingId}}/cancel",
    {"reason": "Postman cancellation test", "seatIds": ["{{secondSeatId}}"]},
)
add(a, "Cancel show", "DELETE", "/shows/{{showId}}")
add(a, "Delete screen", "DELETE", "/theatres/screens/{{screenId}}")
add(a, "Delete theatre", "DELETE", "/theatres/{{theatreId}}")
add(a, "Delete movie", "DELETE", "/movies/{{movieId}}")
variables = dict(
    baseUrl="http://localhost:8080",
    registrationEmail="",
    registrationMobile="9199999999",
    userOtp="",
    adminEmail=manifest["adminEmail"],
    adminOtp="",
    existingUserEmail=manifest["userEmail"],
    existingUserOtp="",
    userToken="",
    adminToken="",
    showDate="",
    waitingSeatId="",
)
variables.update(
    {
        k: manifest[k]
        for k in ("targetUserId", "waitingShowId", "waitingBookingId", "seedBookingId")
    }
)
# derive seeded A1 ID consistently
import uuid

variables["waitingSeatId"] = str(
    uuid.uuid5(
        uuid.NAMESPACE_URL,
        "av-sample/seat/" + str(manifest["counts"]["shows"] * 10 + 1),
    )
)
variables.update({key: "" for key in ("catalogPersonId", "catalogSocialId", "catalogCreditId", "catalogPosterId", "catalogMediaId", "catalogMediaIds")})
collection = {
    "info": {
        "name": "AV Backend — complete ordered API workflow",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        "description": "Run folders in order, skipping optional folder 03. Pause for manual OTP entry. See postman/README.md.",
    },
    "item": folders,
    "event": [
        {
            "listen": "prerequest",
            "script": {
                "type": "text/javascript",
                "exec": [
                    'if(!pm.environment.get("showDate")){ const d=new Date();d.setDate(d.getDate()+31);pm.environment.set("showDate",d.toISOString().slice(0,10)); }'
                ],
            },
        }
    ],
}
(ROOT / "postman/AV-backend.postman_collection.json").write_text(
    json.dumps(collection, indent=2) + "\n"
)
(ROOT / "postman/AV-local.postman_environment.json").write_text(
    json.dumps(
        {
            "name": "AV local",
            "values": [
                {
                    "key": k,
                    "value": v,
                    "enabled": True,
                    "type": "secret" if "Token" in k or "Otp" in k else "default",
                }
                for k, v in variables.items()
            ],
            "_postman_variable_scope": "environment",
        },
        indent=2,
    )
    + "\n"
)
lines = [
    "# Ordered Postman endpoint list",
    "",
    "Import the collection and environment. Follow [README.md](README.md) for setup and manual OTP steps.",
    "",
    "| Step | Request | Method | URL | Token |",
    "| --- | --- | --- | --- | --- |",
]
for n, name, method, url, auth, desc in index:
    lines.append(f"| {n} | {name} | {method} | `{url}` | {auth} |")
(ROOT / "postman/ENDPOINTS.md").write_text("\n".join(lines) + "\n")
print(f"Generated {len(index)} requests in {len(folders)} folders.")
