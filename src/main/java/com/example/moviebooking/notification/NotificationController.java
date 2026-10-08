package com.example.moviebooking.notification;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
@RestController @RequestMapping("/api/v1/notifications")
public class NotificationController { private final NotificationRepository notifications; private final NotificationService service; public NotificationController(NotificationRepository notifications,NotificationService service){this.notifications=notifications;this.service=service;} @GetMapping public List<Notification> mine(Principal p){return notifications.findByUserIdOrderByCreatedAtDesc(p.getName());} @PostMapping("/admin/campaigns") public CampaignResult send(@Valid @RequestBody CampaignRequest request){return service.send(request);} @PostMapping("/admin/shows/{showId}/reminder") public CampaignResult reminder(@PathVariable String showId){return service.sendShowReminder(showId);} }
