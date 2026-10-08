package com.example.moviebooking.notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification,String> { List<Notification> findByUserIdOrderByCreatedAtDesc(String userId); }
