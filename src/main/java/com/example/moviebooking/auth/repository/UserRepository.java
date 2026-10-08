package com.example.moviebooking.auth.repository;
import com.example.moviebooking.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface UserRepository extends JpaRepository<User,String> {
 Optional<User> findByMobile(String mobile);
 boolean existsByMobile(String mobile);
 Optional<User> findByEmailIgnoreCase(String email);
 boolean existsByEmailIgnoreCase(String email);
 List<User> findByEnabledTrue();
}
