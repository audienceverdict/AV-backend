package com.example.moviebooking.catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
public interface MovieRepository extends JpaRepository<Movie,String> {
 Page<Movie> findByStatus(MovieStatus status,Pageable pageable);
}
