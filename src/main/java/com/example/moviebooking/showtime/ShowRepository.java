package com.example.moviebooking.showtime;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.*;
import java.util.*;
public interface ShowRepository extends JpaRepository<Show,String> {
 List<Show> findByMovieIdAndDateGreaterThanEqual(String movieId,LocalDate date);
 List<Show> findByTheatreIdAndDate(String theatreId,LocalDate date);
 @Query("select s from Show s where s.screenId=:screenId and s.date=:date and s.status<>:cancelled and s.startTime < :end and s.endTime > :start")
 List<Show> overlapping(@Param("screenId") String screenId,@Param("date") LocalDate date,@Param("start") LocalTime start,@Param("end") LocalTime end,@Param("cancelled") ShowStatus cancelled);
}
