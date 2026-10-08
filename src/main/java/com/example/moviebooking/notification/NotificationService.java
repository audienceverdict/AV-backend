package com.example.moviebooking.notification;

import com.example.moviebooking.auth.entity.User;
import com.example.moviebooking.auth.provider.BrandedEmailTemplate;
import com.example.moviebooking.auth.provider.EmailProvider;
import com.example.moviebooking.auth.repository.UserRepository;
import com.example.moviebooking.booking.*;
import com.example.moviebooking.catalog.MovieRepository;
import com.example.moviebooking.showtime.ShowRepository;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

@Service public class NotificationService {
 private static final Logger log=LoggerFactory.getLogger(NotificationService.class);
 private final NotificationRepository notifications; private final UserRepository users; private final BookingRepository bookings; private final ShowRepository shows; private final MovieRepository movies; private final EmailProvider email;
 public NotificationService(NotificationRepository notifications,UserRepository users,BookingRepository bookings,ShowRepository shows,MovieRepository movies,EmailProvider email){this.notifications=notifications;this.users=users;this.bookings=bookings;this.shows=shows;this.movies=movies;this.email=email;}
 public String record(String userId,String type,String message){var n=new Notification();n.userId=userId;n.type=type;n.message=message;notifications.save(n);return message;}

 public void sendTicket(User user,Booking booking,String posterUrl){
  String subject=switch(booking.status){case CONFIRMED->"Your ticket for "+booking.movie+" is confirmed";case CANCELLED->"Booking update for "+booking.movie;default->"Booking received for "+booking.movie;};
  try{var content=BrandedEmailTemplate.ticket(user.name,booking,posterUrl);email.sendHtml(user.email,subject,content.plainText(),content.html(),content.inlinePosterDataUri());record(user.id,"BOOKING_"+booking.status.name(),"Email sent: "+subject);}
  catch(RuntimeException e){log.warn("Booking {} saved, but email delivery failed: {}",booking.id,e.getMessage());record(user.id,"BOOKING_EMAIL_DELIVERY_PENDING","Ticket details for "+booking.movie+" are saved in My Bookings. Email delivery can be retried.");}
 }
 public void sendUserMessage(String userId,String type,String subject,String body,String posterUrl){users.findById(userId).filter(user->!blank(user.email)).ifPresent(user->{try{dispatch(user,type,subject,body,posterUrl);}catch(RuntimeException e){log.warn("Email delivery failed for user {}: {}",userId,e.getMessage());record(userId,type+"_EMAIL_PENDING",subject+" Email delivery can be retried.");}});}

 @Transactional public CampaignResult send(CampaignRequest request){
  if(request.target()!=CampaignRequest.Target.ALL_USERS&&request.target()!=CampaignRequest.Target.SHOW_BOOKERS&&(request.movieId()==null||request.movieId().isBlank()))throw new ApiException(400,"MOVIE_REQUIRED","Choose a movie audience.");
  if(request.target()==CampaignRequest.Target.SHOW_BOOKERS&&(request.showId()==null||!shows.existsById(request.showId())))throw new ApiException(404,"SHOW_NOT_FOUND","Choose an existing show.");
  var showIds=request.target()==CampaignRequest.Target.SHOW_BOOKERS?List.of(request.showId()):request.target()==CampaignRequest.Target.MOVIE_BOOKERS?shows.findAll().stream().filter(s->s.movieId.equals(request.movieId())).map(s->s.id).toList():List.<String>of();
  var booked=request.target()==CampaignRequest.Target.ALL_USERS?List.<Booking>of():request.target()==CampaignRequest.Target.SHOW_BOOKERS?bookings.findByShowId(request.showId()):showIds.isEmpty()?List.<Booking>of():bookings.findByShowIdIn(showIds);
  Set<String> ids=booked.stream().filter(b->b.status!=BookingStatus.CANCELLED).map(b->b.userId).collect(Collectors.toSet());
  var recipients=users.findByEnabledTrue().stream().filter(u->request.target()==CampaignRequest.Target.ALL_USERS||ids.contains(u.id)).filter(u->request.status()==null||request.status().isBlank()||booked.stream().anyMatch(b->b.userId.equals(u.id)&&b.status.name().equalsIgnoreCase(request.status()))).toList();
  Comparator<User> comparator=switch(request.sortBy()==null?"name":request.sortBy().toLowerCase(Locale.ROOT)){case "email"->Comparator.comparing(u->Objects.toString(u.email,""),String.CASE_INSENSITIVE_ORDER);case "joined"->Comparator.comparing(u->u.createdAt);case "mobile"->Comparator.comparing(u->u.mobile);default->Comparator.comparing(u->u.name,String.CASE_INSENSITIVE_ORDER);};var sorted=new ArrayList<>(recipients);sorted.sort(comparator);if("desc".equalsIgnoreCase(request.sortDirection()))Collections.reverse(sorted);
  int sent=0,skipped=0;for(User user:sorted){if(blank(user.email)){skipped++;continue;}String body=personalize(request.message(),user,booked);var booking=booked.stream().filter(b->b.userId.equals(user.id)).findFirst().orElse(null);String poster=booking==null?null:posterFor(booking);dispatch(user,"ADMIN_CAMPAIGN",request.subject(),body,poster);sent++;}return new CampaignResult(sorted.size(),sent,skipped,"EMAIL");
 }

 @Transactional public CampaignResult sendShowReminder(String showId){shows.findById(showId).orElseThrow(()->new ApiException(404,"SHOW_NOT_FOUND","Show not found"));var rows=bookings.findByShowId(showId).stream().filter(b->b.status!=BookingStatus.CANCELLED).toList();var request=new CampaignRequest(CampaignRequest.Target.SHOW_BOOKERS,"Reminder: "+rows.stream().findFirst().map(b->b.movie).orElse("your movie"),"Hi {{name}}, a reminder for {{movie}} on {{show_date}} at {{show_time}}. Booking {{booking_id}}.",showId,null,null,"name","asc");return send(request);}

 private void dispatch(User user,String type,String subject,String body,String posterUrl){var content=BrandedEmailTemplate.message(subject,body,posterUrl);email.sendHtml(user.email,subject,content.plainText(),content.html(),content.inlinePosterDataUri());record(user.id,type,truncate(subject+": "+body+" [delivery:email]"));}
 private String posterFor(Booking booking){return shows.findById(booking.showId).flatMap(show->movies.findById(show.movieId)).map(movie->movie.posterUrl).orElse(null);}
 private String personalize(String template,User user,List<Booking> rows){Booking booking=rows.stream().filter(b->b.userId.equals(user.id)).findFirst().orElse(null);return template.replace("{{name}}",safeName(user.name)).replace("{{email}}",Objects.toString(user.email,"")).replace("{{mobile}}",Objects.toString(user.mobile,"")).replace("{{movie}}",booking==null?"":booking.movie).replace("{{show_date}}",booking==null?"":booking.date).replace("{{show_time}}",booking==null?"":booking.time).replace("{{booking_id}}",booking==null?"":booking.id);}
 private String safeName(String name){return name==null||name.isBlank()?"there":name.trim();}private boolean blank(String value){return value==null||value.isBlank();}private String truncate(String value){return value.length()>490?value.substring(0,487)+"...":value;}
}
