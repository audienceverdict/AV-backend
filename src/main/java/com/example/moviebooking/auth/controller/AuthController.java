package com.example.moviebooking.auth.controller;
import com.example.moviebooking.auth.dto.*;
import com.example.moviebooking.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.moviebooking.common.exception.ApiException;
import java.util.Map;
@RestController @RequestMapping("/api/v1/auth") public class AuthController {
 private static final Logger log=LoggerFactory.getLogger(AuthController.class);private final AuthService service;public AuthController(AuthService service){this.service=service;}
 @PostMapping("/email-otp/request") public ResponseEntity<?> requestEmail(@Valid @RequestBody EmailOtpRequest r){try{return ResponseEntity.ok(service.requestEmail(r));}catch(ApiException e){if(e.status<500)throw e;rollbackRequest();return unavailable();}catch(Exception e){log.error("Unexpected failure during email OTP request",e);rollbackRequest();return unavailable();}}
 @PostMapping("/email-otp/verify") public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailOtpVerifyRequest r){try{return ResponseEntity.ok(service.verifyEmail(r));}catch(ApiException e){if(e.status<500)throw e;return unavailable();}catch(Exception e){log.error("Unexpected failure during email OTP verification",e);return unavailable();}}
 @PostMapping("/email-otp/register") public ResponseEntity<?> registerEmail(@Valid @RequestBody EmailOtpRegistrationRequest r){try{return ResponseEntity.ok(service.registerEmail(r));}catch(ApiException e){if(e.status<500)throw e;return unavailable();}catch(Exception e){log.error("Unexpected failure during email OTP registration",e);return unavailable();}}
 private ResponseEntity<Map<String,Object>> unavailable(){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success",false,"message","Unable to send OTP. Please try again later."));}
 private void rollbackRequest(){try{org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();}catch(org.springframework.transaction.NoTransactionException ignored){}}
 @GetMapping("/me") public UserResponse me(Principal p){return service.current(p.getName());}
 @PutMapping("/me") public UserResponse update(Principal p,@Valid @RequestBody UpdateProfileRequest r){return service.update(p.getName(),r);}
}
