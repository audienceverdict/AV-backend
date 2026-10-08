package com.example.moviebooking.auth.repository;
import com.example.moviebooking.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.Optional;
public interface OtpVerificationRepository extends JpaRepository<OtpVerification,Long> {
 Optional<OtpVerification> findFirstByMobileAndChannelOrderByIdDesc(String mobile,String channel);
 long countByMobileAndChannelAndCreatedAtAfter(String mobile,String channel,Instant since);
 long deleteByCreatedAtBefore(Instant cutoff);
}
