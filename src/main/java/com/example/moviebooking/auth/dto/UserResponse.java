package com.example.moviebooking.auth.dto;
import com.example.moviebooking.auth.entity.*;
import java.time.Instant;
public record UserResponse(String id,String name,String mobile,String email,String alternativeMobile,Role role,boolean enabled,Instant createdAt) {
 public static UserResponse of(User u){return new UserResponse(u.id,u.name,u.mobile,u.email,u.alternativeMobile,u.role,u.enabled,u.createdAt);}
}
