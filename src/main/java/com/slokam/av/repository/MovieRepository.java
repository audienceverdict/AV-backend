package com.slokam.av.repository;

import com.slokam.av.entity.Movie;
import com.slokam.av.entity.MovieStatus;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, String> {
    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);
}
