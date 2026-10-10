package com.slokam.av.controller;

import com.slokam.av.dto.*;
import com.slokam.av.entity.*;
import com.slokam.av.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService service;
    public MovieController(MovieService service) { this.service = service; }
    @GetMapping public Page<MovieResponse> list(
        @RequestParam(required = false) MovieStatus status,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String certification,
        @RequestParam(required = false) ReleaseStatus releaseStatus,
        @RequestParam(required = false) ProductionStatus productionStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        return service.list(status, title, genre, language, certification, releaseStatus, productionStatus, page, size);
    }
    @GetMapping("/{id}") public MovieResponse get(@PathVariable String id) { return service.details(id); }
    @PostMapping public MovieResponse create(@Valid @RequestBody MovieRequest movie) { return service.create(movie); }
    @PutMapping("/{id}") public MovieResponse update(@PathVariable String id, @Valid @RequestBody MovieRequest movie) { return service.update(id, movie); }
    @DeleteMapping("/{id}") public void delete(@PathVariable String id) { service.delete(id); }
}
