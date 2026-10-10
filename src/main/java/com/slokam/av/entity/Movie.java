package com.slokam.av.entity;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @Column(length = 36)
    public String id = UUID.randomUUID().toString();

    @Column(nullable = false, length = 200)
    public String title;

    @Column(nullable = false, length = 2000)
    public String posterUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_posters", joinColumns = @JoinColumn(name = "movie_id"))
    @OrderColumn(name = "poster_order")
    @Column(name = "poster_url", nullable = false, columnDefinition = "MEDIUMTEXT")
    public List<String> posterImages = new ArrayList<>();

    @Column(length = 2000)
    public String backdropUrl;

    @Column(length = 2000)
    public String trailerUrl;

    @Column(length = 2000)
    public String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre", length = 80)
    public List<String> genre = new ArrayList<>();

    @Column(nullable = false, length = 80)
    public String language;

    @Column(nullable = false)
    public int duration;

    public LocalDate releaseDate;

    @Column(length = 30)
    public String certification;

    @Column(length = 150)
    public String director;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "cast_name", length = 150)
    public List<String> cast = new ArrayList<>();

    @Column(length = 150)
    public String production;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public MovieStatus status = MovieStatus.UPCOMING;

    @Column(nullable = false)
    public Instant createdAt = Instant.now();

    @Column(nullable = false)
    public Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
