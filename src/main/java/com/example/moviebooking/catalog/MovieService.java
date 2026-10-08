package com.example.moviebooking.catalog;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.util.UUID;
@Service public class MovieService {
 private final MovieRepository movies; private final EntityManager em;
 public MovieService(MovieRepository movies, EntityManager em){this.movies=movies;this.em=em;}
 public Page<Movie> list(MovieStatus status,int page,int size){var p=PageRequest.of(page,Math.min(size,100),Sort.by("releaseDate").descending().and(Sort.by("title")));return status==null?movies.findAll(p):movies.findByStatus(status,p);}
 public Movie get(String id){return movies.findById(id).orElseThrow(()->new ApiException(404,"MOVIE_NOT_FOUND","Movie not found"));}
 @Transactional public Movie create(Movie movie){movie.id=UUID.randomUUID().toString();return movies.save(movie);}
 @Transactional public Movie update(String id,Movie next){var m=get(id);m.title=next.title;m.posterUrl=next.posterUrl;m.posterImages=next.posterImages==null?new java.util.ArrayList<>():new java.util.ArrayList<>(next.posterImages);m.backdropUrl=next.backdropUrl;m.trailerUrl=next.trailerUrl;m.description=next.description;m.genre=next.genre;m.language=next.language;m.duration=next.duration;m.releaseDate=next.releaseDate;m.certification=next.certification;m.director=next.director;m.cast=next.cast;m.production=next.production;m.status=next.status;return m;}
 @Transactional public void delete(String id){
  get(id);
  var shows=em.createNativeQuery("select id from shows where movie_id=?1").setParameter(1,id).getResultList();
  for(Object showId:shows){
   var bookings=em.createNativeQuery("select id from bookings where show_id=?1").setParameter(1,showId).getResultList();
   for(Object bookingId:bookings){em.createNativeQuery("delete from payments where booking_id=?1").setParameter(1,bookingId).executeUpdate();em.createNativeQuery("delete from booking_seats where booking_id=?1").setParameter(1,bookingId).executeUpdate();}
   em.createNativeQuery("delete from bookings where show_id=?1").setParameter(1,showId).executeUpdate();
  }
  em.createNativeQuery("delete from shows where movie_id=?1").setParameter(1,id).executeUpdate();
  em.createNativeQuery("delete from reviews where movie_id=?1").setParameter(1,id).executeUpdate();
  em.createNativeQuery("delete from movie_genres where movie_id=?1").setParameter(1,id).executeUpdate();
  em.createNativeQuery("delete from movie_cast where movie_id=?1").setParameter(1,id).executeUpdate();
  em.createNativeQuery("delete from movies where id=?1").setParameter(1,id).executeUpdate();
 }
}
