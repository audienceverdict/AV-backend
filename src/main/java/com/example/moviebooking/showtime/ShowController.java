package com.example.moviebooking.showtime;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api/v1/shows")
public class ShowController {
 private final ShowService service;
 public ShowController(ShowService service){this.service=service;}
 @GetMapping public List<Show> list(@RequestParam(required=false) String movieId,@RequestParam(required=false) String theatreId,@RequestParam(required=false) LocalDate date){return service.list(movieId,theatreId,date);}
 @GetMapping("/{id}") public Show get(@PathVariable String id){return service.get(id);}
 @GetMapping("/{id}/availability") public Map<String,Object> availability(@PathVariable String id){return service.availability(id);}
 @PostMapping public Show create(@RequestBody Show show){return service.create(show);}
 @PutMapping("/{id}") public Show update(@PathVariable String id,@RequestBody Show show){return service.update(id,show);}
 @DeleteMapping("/{id}") public void cancel(@PathVariable String id){service.cancel(id);}
}
