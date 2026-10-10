package com.slokam.av.controller;
import com.slokam.av.dto.*;
import com.slokam.av.service.MovieMediaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/media")
public class MovieMediaController {
    private final MovieMediaService service;
    public MovieMediaController(MovieMediaService service) { this.service = service; }
    @GetMapping public List<MovieMediaResponse> list(@PathVariable String movieId) { return service.list(movieId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public MovieMediaResponse add(@PathVariable String movieId, @Valid @RequestBody MovieMediaRequest r) { return service.add(movieId, r); }
    @PutMapping("/{mediaId}") public MovieMediaResponse update(@PathVariable String movieId, @PathVariable String mediaId, @Valid @RequestBody MovieMediaRequest r) { return service.update(movieId, mediaId, r); }
    @PutMapping("/order") public List<MovieMediaResponse> order(@PathVariable String movieId, @Valid @RequestBody MediaOrderRequest r) { return service.reorder(movieId, r.mediaIds); }
    @PutMapping("/{mediaId}/primary") public MovieMediaResponse primary(@PathVariable String movieId, @PathVariable String mediaId) { return service.primary(movieId, mediaId); }
    @DeleteMapping("/{mediaId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String movieId, @PathVariable String mediaId) { service.delete(movieId, mediaId); }
}
