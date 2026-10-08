package com.example.moviebooking.venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ScreenRepository extends JpaRepository<Screen,String> {
 List<Screen> findByTheatreId(String theatreId);
}
