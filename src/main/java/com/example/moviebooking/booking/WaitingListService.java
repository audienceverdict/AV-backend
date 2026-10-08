package com.example.moviebooking.booking;
import com.example.moviebooking.common.exception.ApiException;
import com.example.moviebooking.showtime.*;
import com.example.moviebooking.venue.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.security.Principal;
import java.util.*;
@Service public class WaitingListService {
 private final WaitingListRepository entries; private final ShowRepository shows; private final ShowService showService; @Value("${booking.waiting-list-offer-minutes:5}") private long offerMinutes;
 public WaitingListService(WaitingListRepository entries,ShowRepository shows,ShowService showService){this.entries=entries;this.shows=shows;this.showService=showService;}
 public List<WaitingListEntry> mine(Principal p){return entries.findAll().stream().filter(e->e.userId.equals(p.getName())).sorted(Comparator.comparing((WaitingListEntry e)->e.createdAt,Comparator.reverseOrder())).toList();}
 public List<WaitingListEntry> all(){return entries.findAll().stream().sorted(Comparator.comparing((WaitingListEntry e)->e.showId).thenComparingInt(e->e.position)).toList();}
 @Transactional public WaitingListEntry join(Principal p,WaitingListRequest r){var show=shows.findById(r.showId).orElseThrow(()->new ApiException(404,"SHOW_NOT_FOUND","Show not found"));if(show.status!=ShowStatus.OPEN)throw new ApiException(400,"SHOW_CLOSED","Show is not open for booking");if(r.requestedSeatsCount<1||r.requestedSeatsCount>show.maxTicketsPerMobile)throw new ApiException(400,"INVALID_TICKET_COUNT","Requested tickets exceed the screening limit");var active=entries.findAll().stream().filter(e->e.showId.equals(show.id)&&(e.status==WaitingListStatus.WAITING||e.status==WaitingListStatus.OFFERED)).toList();if(active.stream().anyMatch(e->e.userId.equals(p.getName())))throw new ApiException(409,"ALREADY_WAITING","You are already on this waiting list");if(availableSeatCount(show.id)==0){var e=new WaitingListEntry();e.showId=show.id;e.userId=p.getName();e.requestedSeatsCount=r.requestedSeatsCount;e.position=active.stream().mapToInt(x->x.position).max().orElse(0)+1;return entries.save(e);}throw new ApiException(409,"SEATS_AVAILABLE","Seats are still available. Please book directly instead of joining the waiting list.");}
 private long availableSeatCount(String showId){var availability=showService.availability(showId);var layout=(Map<?,?>)availability.get("layout");Object value=layout.get("seats");var seats=value instanceof List<?> list?list:List.of();var unavailable=new HashSet<String>();Object blocked=availability.get("bookedSeatIds");if(blocked instanceof Collection<?> ids)ids.forEach(id->unavailable.add(String.valueOf(id)));return seats.stream().filter(seat->{if(seat instanceof LayoutSeat s)return !s.disabled&&!unavailable.contains(s.id);if(seat instanceof Seat s)return !s.disabled&&!unavailable.contains(s.id);return false;}).count();}
 @Transactional public void cancel(String id,Principal p){var e=entries.findById(id).orElseThrow(()->new ApiException(404,"WAITING_LIST_NOT_FOUND","Waiting-list entry not found"));if(!e.userId.equals(p.getName()))throw new ApiException(403,"FORBIDDEN","Access denied");e.status=WaitingListStatus.CANCELLED;}
 @Transactional public WaitingListEntry claim(String id,Principal p){var e=entries.findById(id).orElseThrow(()->new ApiException(404,"WAITING_LIST_NOT_FOUND","Waiting-list entry not found"));if(!e.userId.equals(p.getName()))throw new ApiException(403,"FORBIDDEN","Access denied");if(e.status!=WaitingListStatus.OFFERED||e.offerExpiresAt==null||e.offerExpiresAt.isBefore(Instant.now()))throw new ApiException(409,"OFFER_EXPIRED","This waiting-list offer has expired");e.status=WaitingListStatus.CONVERTED;return e;}
 @Scheduled(fixedDelay=30000) @Transactional public void expireOffers(){var now=Instant.now();entries.findAll().stream().filter(e->e.status==WaitingListStatus.OFFERED&&e.offerExpiresAt!=null&&e.offerExpiresAt.isBefore(now)).forEach(e->e.status=WaitingListStatus.EXPIRED);}
}
