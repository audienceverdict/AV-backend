package com.example.moviebooking.auth.service;
import com.example.moviebooking.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.*;
@Service public class CustomUserDetailsService implements UserDetailsService {
 private final UserRepository users; public CustomUserDetailsService(UserRepository users){this.users=users;}
 public UserDetails loadUserByUsername(String id){var u=users.findById(id).orElseThrow(()->new UsernameNotFoundException("Account unavailable"));return org.springframework.security.core.userdetails.User.withUsername(u.id).password("").roles(u.role.name()).disabled(!u.enabled).build();}
}
