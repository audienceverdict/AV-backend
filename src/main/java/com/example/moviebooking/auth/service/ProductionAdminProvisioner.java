package com.example.moviebooking.auth.service;

import com.example.moviebooking.auth.entity.Role;
import com.example.moviebooking.auth.entity.User;
import com.example.moviebooking.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Component
@Profile("prod")
public class ProductionAdminProvisioner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ProductionAdminProvisioner.class);

    private final UserRepository users;
    private final String adminEmail;

    public ProductionAdminProvisioner(UserRepository users,
                                      @Value("${app.admin.email:}") String adminEmail) {
        this.users = users;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank()) {
            log.warn("Production admin account was not provisioned because app.admin.email is empty");
            return;
        }

        var existing = users.findByEmailIgnoreCase(adminEmail);
        User user;
        boolean changed;
        if (existing.isPresent()) {
            user = existing.get();
            changed = false;
        } else {
            user = new User();
            user.name = "Administrator";
            user.email = adminEmail;
            // User.mobile is required and unique in the current schema. Admins authenticate by email OTP.
            user.mobile = "ADMIN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            changed = true;
        }

        if (user.role != Role.ADMIN) {
            user.role = Role.ADMIN;
            changed = true;
        }
        if (!user.enabled) {
            user.enabled = true;
            changed = true;
        }
        if (changed) {
            users.save(user);
        }
        log.info("Provisioned configured production admin account {}", adminEmail);
    }
}
