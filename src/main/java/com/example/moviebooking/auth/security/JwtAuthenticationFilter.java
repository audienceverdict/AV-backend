package com.example.moviebooking.auth.security;
import com.example.moviebooking.auth.service.CustomUserDetailsService;
import com.example.moviebooking.common.exception.ApiErrors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
@Component public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final CustomUserDetailsService users; private final ObjectMapper mapper;
 public JwtAuthenticationFilter(JwtService jwt,CustomUserDetailsService users,ObjectMapper mapper){this.jwt=jwt;this.users=users;this.mapper=mapper;}
 protected boolean shouldNotFilter(HttpServletRequest r){return "OPTIONS".equals(r.getMethod())||("POST".equals(r.getMethod())&&(r.getRequestURI().equals("/api/v1/auth/email-otp/request")||r.getRequestURI().equals("/api/v1/auth/email-otp/verify")||r.getRequestURI().equals("/api/v1/auth/email-otp/register")));}
 protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException {
 String header=request.getHeader("Authorization");
 if(header!=null){try{if(!header.startsWith("Bearer "))throw new IllegalArgumentException();var token=jwt.validate(header.substring(7));var user=users.loadUserByUsername(token.getSubject());if(!user.isEnabled())throw new IllegalArgumentException();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()));}
 catch(org.springframework.security.oauth2.jwt.JwtException|org.springframework.security.core.AuthenticationException|IllegalArgumentException e){SecurityContextHolder.clearContext();response.setStatus(401);response.setContentType("application/json");mapper.writeValue(response.getOutputStream(),ApiErrors.of(401,"UNAUTHORIZED","Authentication required",request.getRequestURI()));return;}}
 chain.doFilter(request,response);
 }
}
