package com.slokam.av.exception.handler;

import com.slokam.av.exception.ApiErrors;
import com.slokam.av.exception.custom.ApiException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiErrors> api(ApiException e, HttpServletRequest r) {
        if (e.status >= 500) log.error("API operation failed status={} code={}", e.status, e.code);
        else log.warn("API request rejected status={} code={}", e.status, e.code);
        return ResponseEntity.status(e.status)
                .body(ApiErrors.of(e.status, e.code, e.getMessage(), r.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrors> validation(MethodArgumentNotValidException e, HttpServletRequest r) {
        log.warn("Request validation failed fieldErrorCount={}", e.getBindingResult().getFieldErrorCount());
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(
                        new ApiErrors(
                                Instant.now(),
                                400,
                                "VALIDATION_ERROR",
                                "Request validation failed",
                                r.getRequestURI(),
                                fields));
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrors> invalidParameter(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e, HttpServletRequest r) {
        return ResponseEntity.badRequest().body(new ApiErrors(Instant.now(), 400, "VALIDATION_ERROR",
                "Invalid request parameter", r.getRequestURI(), Map.of(e.getName(), "Invalid value")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiErrors> malformed(HttpServletRequest r) {
        log.warn("Request rejected: malformed body");
        return ResponseEntity.badRequest()
                .body(
                        ApiErrors.of(
                                400,
                                "MALFORMED_REQUEST",
                                "Invalid request body",
                                r.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiErrors> duplicate(HttpServletRequest r) {
        log.warn("Request rejected: data integrity conflict");
        if (r.getRequestURI().contains("/seat-holds"))
            return ResponseEntity.status(409)
                    .body(
                            ApiErrors.of(
                                    409,
                                    "SEAT_UNAVAILABLE",
                                    "One or more seats are currently unavailable",
                                    r.getRequestURI()));
        return ResponseEntity.status(409)
                .body(
                        ApiErrors.of(
                                409,
                                "DUPLICATE_VALUE",
                                r.getRequestURI().startsWith("/api/v1/movies") || r.getRequestURI().startsWith("/api/v1/people")
                                        ? "Catalog value conflicts with an existing record" : "Mobile or email is already in use",
                                r.getRequestURI()));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    ResponseEntity<ApiErrors> forbidden(HttpServletRequest r) {
        log.warn("Request rejected: access denied");
        return ResponseEntity.status(403)
                .body(ApiErrors.of(403, "FORBIDDEN", "Access denied", r.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    ResponseEntity<ApiErrors> unauthorized(HttpServletRequest r) {
        log.warn("Request rejected: authentication required");
        return ResponseEntity.status(401)
                .body(ApiErrors.of(401, "UNAUTHORIZED", "Authentication required", r.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrors> unexpected(Exception e, HttpServletRequest r) {
        if (e instanceof org.springframework.web.ErrorResponse response) {
            int status = response.getStatusCode().value();
            if (status >= 500) log.error("Framework request failure status={} failureType={}", status, e.getClass().getName());
            else log.warn("Framework request rejected status={} failureType={}", status, e.getClass().getName());
            var httpStatus = org.springframework.http.HttpStatus.resolve(status);
            return ResponseEntity.status(status).body(ApiErrors.of(status, "REQUEST_ERROR",
                    httpStatus == null ? "Request failed" : httpStatus.getReasonPhrase(), r.getRequestURI()));
        }
        // Exception messages can include SQL values, email content, or credentials.
        log.error("Unexpected request failure failureType={}", e.getClass().getName());
        for (StackTraceElement frame : e.getStackTrace()) {
            log.error("    at {}", frame);
        }
        return ResponseEntity.internalServerError()
                .body(ApiErrors.of(500, "INTERNAL_ERROR", "An unexpected error occurred", r.getRequestURI()));
    }
}

