package com.example.moviebooking.review;
import com.example.moviebooking.auth.repository.UserRepository;
import com.example.moviebooking.catalog.MovieRepository;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.Principal;
import java.util.*;
@Service public class ReviewService {
 private final ReviewRepository reviews; private final MovieRepository movies; private final UserRepository users;
 public ReviewService(ReviewRepository reviews,MovieRepository movies,UserRepository users){this.reviews=reviews;this.movies=movies;this.users=users;}
 public List<Review> list(String movieId,ReviewStatus status){return movieId==null?reviews.findByStatusOrderByCreatedAtDesc(status):reviews.findByMovieIdAndStatusOrderByCreatedAtDesc(movieId,status);}
 @Transactional public Review create(Principal p,Review r){movies.findById(r.movieId).orElseThrow(()->new ApiException(404,"MOVIE_NOT_FOUND","Movie not found"));var u=users.findById(p.getName()).orElseThrow();r.id=UUID.randomUUID().toString();r.userId=u.id;r.author=r.author==null||r.author.isBlank()?u.name:r.author;r.status=ReviewStatus.PENDING;r.isHighlighted=false;validate(r);return reviews.save(r);}
 @Transactional public Review update(String id,Principal p,Review next){var r=reviews.findById(id).orElseThrow(()->new ApiException(404,"REVIEW_NOT_FOUND","Review not found"));if(!Objects.equals(r.userId,p.getName()))throw new ApiException(403,"FORBIDDEN","Access denied");r.title=next.title;r.text=next.text;r.rating=next.rating;r.videoUrl=next.videoUrl;r.platform=next.platform;r.thumbnail=next.thumbnail;r.status=ReviewStatus.PENDING;validate(r);return r;}
 @Transactional public Review moderate(String id,ReviewStatus status,boolean highlighted){var r=reviews.findById(id).orElseThrow(()->new ApiException(404,"REVIEW_NOT_FOUND","Review not found"));r.status=status;r.isHighlighted=highlighted;return r;}
 private void validate(Review r){if(r.rating<1||r.rating>5)throw new ApiException(400,"INVALID_RATING","Rating must be between 1 and 5");}
}
