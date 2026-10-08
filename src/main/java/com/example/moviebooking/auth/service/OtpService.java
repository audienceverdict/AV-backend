package com.example.moviebooking.auth.service;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.*;
import com.example.moviebooking.auth.dto.OtpResponse;
import com.example.moviebooking.auth.provider.BrandedEmailTemplate;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import java.security.SecureRandom;
import java.time.Instant;
@Service public class OtpService {
 private final OtpVerificationRepository records; private final AuthLockRepository locks; private final com.example.moviebooking.auth.provider.EmailProvider email;
 private final BCryptPasswordEncoder hashes=new BCryptPasswordEncoder(); private final SecureRandom random=new SecureRandom();
 private final long expiry,cooldown; private final int attempts,requests;
 public OtpService(OtpVerificationRepository records,AuthLockRepository locks,com.example.moviebooking.auth.provider.EmailProvider email,
 @Value("${auth.otp.expiration-seconds:300}") long expiry,@Value("${auth.otp.resend-cooldown-seconds:30}") long cooldown,
 @Value("${auth.otp.max-attempts:5}") int attempts,@Value("${auth.otp.max-requests-per-hour:10}") int requests){
 this.records=records;this.locks=locks;this.email=email;this.expiry=expiry;this.cooldown=cooldown;this.attempts=attempts;this.requests=requests;
 if(expiry<1||cooldown<1||attempts<1||requests<1)throw new IllegalArgumentException("OTP limits must be positive");}
 private void lock(String destination){locks.lock(Math.floorMod(destination.hashCode(),64));}
 @Transactional(noRollbackFor=ApiException.class) public OtpResponse requestEmail(String address){ try{return requestCode(address);}catch(ApiException e){if(e.status>=500)org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();throw e;} }
 private OtpResponse requestCode(String destination){
 String channel="EMAIL";
 lock(destination);Instant now=Instant.now();
 var latest=records.findFirstByMobileAndChannelOrderByIdDesc(destination,channel);
 if(latest.isPresent()&&latest.get().verified)latest.get().verified=false;
 if(latest.isPresent()&&latest.get().createdAt.plusSeconds(cooldown).isAfter(now))throw new ApiException(429,"OTP_COOLDOWN","Please wait before requesting another code");
 if(records.countByMobileAndChannelAndCreatedAtAfter(destination,channel,now.minusSeconds(3600))>=requests)throw new ApiException(429,"OTP_RATE_LIMIT","Too many requests. Please try again later.");
 String code=String.format("%06d",random.nextInt(1000000));
 OtpVerification otp=new OtpVerification();otp.mobile=destination;otp.channel=channel;otp.otpHash=hashes.encode(code);otp.createdAt=now;otp.expiresAt=now.plusSeconds(expiry);
 records.saveAndFlush(otp);var message=BrandedEmailTemplate.otp(code,expiry/60);email.sendHtml(destination,"Your Audience Verdict sign-in code",message.plainText(),message.html(),null);
 return new OtpResponse(true,"If this email can sign in, a verification code has been sent",expiry,cooldown);
 }
 // Called inside AuthService's transaction. Invalid attempts must commit as well.
 public void verifyEmail(String address,String code){ verify(address,code); }
 private void verify(String destination,String code){
 String channel="EMAIL";
 lock(destination);var otp=records.findFirstByMobileAndChannelOrderByIdDesc(destination,channel).orElseThrow(()->invalid());
 if(otp.verified||!otp.expiresAt.isAfter(Instant.now()))throw invalid();
 if(otp.attemptCount>=attempts)throw new ApiException(429,"OTP_ATTEMPT_LIMIT","Too many attempts. Request a new code.");
 otp.attemptCount++;records.save(otp);
 if(!hashes.matches(code,otp.otpHash))throw invalid();
 otp.verified=true;records.save(otp);
 }
 @Transactional public void requireVerifiedEmail(String address){lock(address);var otp=records.findFirstByMobileAndChannelOrderByIdDesc(address,"EMAIL").orElseThrow(()->invalid());if(!otp.verified||!otp.expiresAt.isAfter(Instant.now()))throw invalid();}
 @Transactional public void consumeVerifiedEmail(String address){requireVerifiedEmail(address);var otp=records.findFirstByMobileAndChannelOrderByIdDesc(address,"EMAIL").orElseThrow(()->invalid());otp.verified=false;records.save(otp);}
 private ApiException invalid(){return new ApiException(400,"INVALID_OTP","The code is invalid or expired");}
 // Keep request history beyond the hourly rate-limit window, even for expired codes.
 @Scheduled(fixedDelay=3600000) @Transactional public void cleanup(){records.deleteByCreatedAtBefore(Instant.now().minusSeconds(Math.max(86400,expiry+3600)));}
}
