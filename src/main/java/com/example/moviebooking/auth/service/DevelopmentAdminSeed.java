package com.example.moviebooking.auth.service;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.UserRepository;
@Component @Profile("dev") public class DevelopmentAdminSeed implements ApplicationRunner {
 private static final Logger log=LoggerFactory.getLogger(DevelopmentAdminSeed.class);
 private final UserRepository users; private final MobileNormalizer normalizer; private final String mobile,email;
 public DevelopmentAdminSeed(UserRepository users,MobileNormalizer normalizer,@Value("${app.admin.mobile:}") String mobile,@Value("${app.admin.email:}") String email){this.users=users;this.normalizer=normalizer;this.mobile=mobile;this.email=email;}
 @Transactional public void run(ApplicationArguments args){if(mobile.isBlank())return;if(email.isBlank()){log.warn("ADMIN_MOBILE is configured but ADMIN_EMAIL is missing; development admin seed was skipped");return;}String number=normalizer.normalize(mobile);String address=email.trim().toLowerCase(java.util.Locale.ROOT);var u=users.findByMobile(number).orElseGet(()->{var fresh=new User();fresh.mobile=number;fresh.name="Administrator";return fresh;});users.findByEmailIgnoreCase(address).filter(other->!other.id.equals(u.id)).ifPresent(other->{throw new IllegalStateException("ADMIN_EMAIL is already assigned to another account");});u.email=address;u.role=Role.ADMIN;users.save(u);}
}
