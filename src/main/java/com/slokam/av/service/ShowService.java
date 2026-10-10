package com.slokam.av.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.slokam.av.entity.ActiveStatus;
import com.slokam.av.entity.BookingStatus;
import com.slokam.av.entity.MovieStatus;
import com.slokam.av.entity.SeatHoldStatus;
import com.slokam.av.entity.Show;
import com.slokam.av.entity.ShowStatus;
import com.slokam.av.exception.custom.ApiException;
import com.slokam.av.repository.BookingRepository;
import com.slokam.av.repository.MovieRepository;
import com.slokam.av.repository.ScreenRepository;
import com.slokam.av.repository.SeatHoldRepository;
import com.slokam.av.repository.SeatRepository;
import com.slokam.av.repository.ShowRepository;
import com.slokam.av.repository.TheatreRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class ShowService {
    private static final Logger log = LoggerFactory.getLogger(ShowService.class);
    private final ShowRepository shows;
    private final MovieRepository movies;
    private final TheatreRepository theatres;
    private final ScreenRepository screens;
    private final SeatRepository seats;
    private final BookingRepository bookings;
    private final SeatHoldRepository holds;
    private final TheatreService layouts;

    public ShowService(
            ShowRepository shows,
            MovieRepository movies,
            TheatreRepository theatres,
            ScreenRepository screens,
            SeatRepository seats,
            BookingRepository bookings,
            SeatHoldRepository holds,
            TheatreService layouts) {
        this.shows = shows;
        this.movies = movies;
        this.theatres = theatres;
        this.screens = screens;
        this.seats = seats;
        this.bookings = bookings;
        this.holds = holds;
        this.layouts = layouts;
    }

    public List<Show> list(String movieId, String theatreId, LocalDate date) {
        log.debug("Processing ShowService.list");
        var result =
                movieId != null
                        ? shows.findByMovieIdAndDateGreaterThanEqual(
                                movieId, date == null ? LocalDate.now() : date)
                        : theatreId != null && date != null
                                ? shows.findByTheatreIdAndDate(theatreId, date)
                                : shows.findAll();
        return result.stream().filter(show -> show.status != ShowStatus.CANCELLED).toList();
    }

    public Show get(String id) {
        log.debug("Processing ShowService.get");
        return shows.findById(id)
                .orElseThrow(() -> new ApiException(404, "SHOW_NOT_FOUND", "Show not found"));
    }

    public Map<String, Object> availability(String id) {
        log.debug("Processing ShowService.availability");
        var show = get(id);
        var screen =
                screens.findById(show.screenId)
                        .orElseThrow(
                                () ->
                                        new ApiException(
                                                404, "SCREEN_NOT_FOUND", "Screen not found"));
        var booked =
                bookings.findBookedSeatIds(
                        id, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
        var held =
                holds
                        .findByShowIdAndStatusAndExpiresAtAfter(
                                id, SeatHoldStatus.ACTIVE, java.time.Instant.now())
                        .stream()
                        .map(h -> h.seatId)
                        .toList();
        var unavailable = new LinkedHashSet<String>(booked);
        unavailable.addAll(held);
        var version =
                show.layoutVersionId == null
                        ? null
                        : layouts.layoutDetails(show.screenId, show.layoutVersionId);
        if (version == null || ((List<?>) version.getOrDefault("seats", List.of())).isEmpty())
            version = Map.of("seats", screen.seats);
        return Map.of(
                "show", show, "screen", screen, "layout", version, "bookedSeatIds", unavailable);
    }

    @Transactional
    public Show create(Show s) {
        log.debug("Processing ShowService.create");
        validateRelations(s);
        validateOverlap(null, s);
        var active = layouts.activeLayout(s.screenId);
        s.layoutVersionId = active.id;
        s.layoutVersion = active.versionNumber;
        s.id = UUID.randomUUID().toString();
        log.info("Creating show showId={}", s.id);
        return shows.save(s);
    }

    @Transactional
    public Show update(String id, Show next) {
        log.debug("Processing ShowService.update");
        var s = get(id);
        next.id = id;
        validateRelations(next);
        validateOverlap(id, next);
        if (!Objects.equals(s.screenId, next.screenId))
            throw new ApiException(
                    400,
                    "SHOW_SCREEN_IMMUTABLE",
                    "Create a new show to change screens or layout versions");
        s.movieId = next.movieId;
        s.theatreId = next.theatreId;
        s.date = next.date;
        s.seatPrices = next.seatPrices;
        s.startTime = next.startTime;
        s.endTime = next.endTime;
        s.ticketType = next.ticketType;
        s.ticketPrice = next.ticketPrice;
        s.maxTicketsPerMobile = next.maxTicketsPerMobile;
        s.requireAdminConfirmation = next.requireAdminConfirmation;
        s.bookingOpens = next.bookingOpens;
        s.bookingCloses = next.bookingCloses;
        s.status = next.status;
        log.info("Show updated showId={} status={}", id, s.status);
        return s;
    }

    @Transactional
    public void cancel(String id) {
        log.debug("Processing ShowService.cancel");
        var show = get(id);
        if (bookings.countByShowId(id) == 0) {
            shows.delete(show);
            log.info("Show deleted showId={}", id);
        } else {
            show.status = ShowStatus.CANCELLED;
            log.info("Show cancelled showId={}", id);
        }
    }

    private void validateRelations(Show s) {
        movies.findById(s.movieId)
                .filter(m -> m.status != MovieStatus.ENDED)
                .orElseThrow(() -> new ApiException(400, "INVALID_MOVIE", "Movie is unavailable"));
        theatres.findById(s.theatreId)
                .filter(t -> t.status == ActiveStatus.ACTIVE)
                .orElseThrow(
                        () -> new ApiException(400, "INVALID_THEATRE", "Theatre is unavailable"));
        screens.findById(s.screenId)
                .filter(sc -> sc.status == ActiveStatus.ACTIVE && sc.theatreId.equals(s.theatreId))
                .orElseThrow(
                        () -> new ApiException(400, "INVALID_SCREEN", "Screen is unavailable"));
    }

    private void validateOverlap(String id, Show s) {
        if (!s.startTime.isBefore(s.endTime))
            throw new ApiException(400, "INVALID_TIME", "Show start time must be before end time");
        var overlaps =
                shows
                        .overlapping(
                                s.screenId, s.date, s.startTime, s.endTime, ShowStatus.CANCELLED)
                        .stream()
                        .anyMatch(o -> !o.id.equals(id));
        if (overlaps)
            throw new ApiException(
                    409, "SHOW_OVERLAP", "Screen already has a show in this time range");
    }
}
