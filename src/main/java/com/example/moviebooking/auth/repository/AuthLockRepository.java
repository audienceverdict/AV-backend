package com.example.moviebooking.auth.repository;
import com.example.moviebooking.auth.entity.AuthLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
public interface AuthLockRepository extends JpaRepository<AuthLock,Integer> {
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select l from AuthLock l where l.id = :id") AuthLock lock(int id);
}
