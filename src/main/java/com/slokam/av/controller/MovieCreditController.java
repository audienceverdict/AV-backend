package com.slokam.av.controller;
import com.slokam.av.dto.*;
import com.slokam.av.service.MovieCreditService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;

@RestController
@RequestMapping("/api/v1/movies/{movieId}")
public class MovieCreditController {
    private final MovieCreditService service;
    public MovieCreditController(MovieCreditService service) { this.service = service; }
    @GetMapping("/credits") public List<MovieCreditResponse> list(@PathVariable String movieId) { return service.list(movieId); }
    @GetMapping("/cast") public List<MovieCreditResponse> cast(@PathVariable String movieId) { return service.cast(movieId); }
    @GetMapping("/crew") public Map<String, List<MovieCreditResponse>> crew(@PathVariable String movieId) { return service.crew(movieId); }
    @PostMapping("/credits") @ResponseStatus(HttpStatus.CREATED)
    public MovieCreditResponse add(@PathVariable String movieId, @Valid @RequestBody MovieCreditRequest r) { return service.add(movieId, r); }
    @PutMapping("/credits/{creditId}") public MovieCreditResponse update(@PathVariable String movieId, @PathVariable String creditId, @Valid @RequestBody MovieCreditRequest r) { return service.update(movieId, creditId, r); }
    @DeleteMapping("/credits/{creditId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String movieId, @PathVariable String creditId) { service.delete(movieId, creditId); }
}
