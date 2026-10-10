package com.slokam.av.controller;

import com.slokam.av.entity.Review;
import com.slokam.av.entity.ReviewStatus;
import com.slokam.av.service.ReviewService;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<Review> list(
            @RequestParam(required = false) String movieId,
            @RequestParam(defaultValue = "APPROVED") ReviewStatus status) {
        return service.list(movieId, status);
    }

    @PostMapping
    public Review create(Principal p, @RequestBody Review review) {
        return service.create(p, review);
    }

    @PutMapping("/{id}")
    public Review update(@PathVariable String id, Principal p, @RequestBody Review review) {
        return service.update(id, p, review);
    }

    @PostMapping("/admin/{id}/moderate")
    public Review moderate(
            @PathVariable String id,
            @RequestParam ReviewStatus status,
            @RequestParam(defaultValue = "false") boolean highlighted) {
        return service.moderate(id, status, highlighted);
    }
}
