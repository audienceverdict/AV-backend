package com.example.moviebooking.common.exception;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) ResponseEntity<ApiErrors> api(ApiException e,HttpServletRequest r){return ResponseEntity.status(e.status).body(ApiErrors.of(e.status,e.code,e.getMessage(),r.getRequestURI()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiErrors> validation(MethodArgumentNotValidException e,HttpServletRequest r){Map<String,String> fields=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(f->fields.putIfAbsent(f.getField(),f.getDefaultMessage()));return ResponseEntity.badRequest().body(new ApiErrors(Instant.now(),400,"VALIDATION_ERROR","Request validation failed",r.getRequestURI(),fields));}
 @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ApiErrors> malformed(HttpServletRequest r){return ResponseEntity.badRequest().body(ApiErrors.of(400,"MALFORMED_REQUEST","Invalid request body",r.getRequestURI()));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiErrors> duplicate(HttpServletRequest r){if(r.getRequestURI().contains("/seat-holds"))return ResponseEntity.status(409).body(ApiErrors.of(409,"SEAT_UNAVAILABLE","One or more seats are currently unavailable",r.getRequestURI()));return ResponseEntity.status(409).body(ApiErrors.of(409,"DUPLICATE_VALUE","Mobile or email is already in use",r.getRequestURI()));}
}
