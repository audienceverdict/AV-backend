package com.example.moviebooking.review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ReviewRepository extends JpaRepository<Review,String> {
 List<Review> findByMovieIdAndStatusOrderByCreatedAtDesc(String movieId,ReviewStatus status);
 List<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status);
}
