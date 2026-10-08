package com.example.moviebooking.auth.service;

import com.example.moviebooking.auth.entity.Role;
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

        users.findByEmailIgnoreCase(adminEmail).ifPresentOrElse(user -> {
            user.role = Role.ADMIN;
            user.enabled = true;
            users.save(user);
            log.info("Enabled configured production admin account {}", adminEmail);
        }, () -> log.warn("Configured production admin email {} has no user account; create the account before admin login", adminEmail));
    }
}
