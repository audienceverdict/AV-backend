package com.slokam.av.controller;

import com.slokam.av.entity.Movie;
import com.slokam.av.entity.MovieStatus;
import com.slokam.av.service.MovieService;

import jakarta.validation.Valid;

import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public Page<Movie> list(
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(status, page, size);
    }

    @GetMapping("/{id}")
    public Movie get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    public Movie create(@Valid @RequestBody Movie movie) {
        return service.create(movie);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable String id, @Valid @RequestBody Movie movie) {
        return service.update(id, movie);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
