package com.example.moviebooking;

import com.example.moviebooking.auth.entity.Role;
import com.example.moviebooking.auth.entity.User;
import com.example.moviebooking.auth.repository.UserRepository;
import com.example.moviebooking.auth.service.ProductionAdminProvisioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"prod", "test"})
class ProductionAdminProvisionerTest {
    @Autowired
    UserRepository users;

    @Autowired
    ProductionAdminProvisioner provisioner;

    @Value("${app.admin.email:}")
    String configuredAdminEmail;

    @BeforeEach
    void clearUsers() {
        users.deleteAll();
    }

    @Test
    void promotesAndEnablesExistingConfiguredAccount() {
        User existing = new User();
        existing.name = "Configured Admin";
        existing.mobile = "+919876543210";
        existing.email = configuredAdminEmail;
        existing.role = Role.USER;
        existing.enabled = false;
        users.saveAndFlush(existing);

        provision();

        User admin = users.findByEmailIgnoreCase(configuredAdminEmail).orElseThrow();
        assertEquals(Role.ADMIN, admin.role);
        assertTrue(admin.enabled);
        assertEquals(existing.mobile, admin.mobile);
        assertEquals(1, users.count());
    }

    @Test
    void createsConfiguredAccountAndRemainsIdempotentOnRestart() {
        provision();

        User admin = users.findByEmailIgnoreCase(configuredAdminEmail).orElseThrow();
        assertEquals(Role.ADMIN, admin.role);
        assertTrue(admin.enabled);
        assertEquals("Administrator", admin.name);
        assertNotNull(admin.mobile);
        assertTrue(admin.mobile.length() <= 20);

        String id = admin.id;
        provision();
        assertEquals(1, users.count());
        assertEquals(id, users.findByEmailIgnoreCase(configuredAdminEmail).orElseThrow().id);
    }

    @Test
    void leavesOtherNormalUsersUnchanged() {
        User normal = new User();
        normal.name = "Regular User";
        normal.mobile = "+919876543211";
        normal.email = "regular@example.com";
        users.saveAndFlush(normal);

        provision();

        User unchanged = users.findByEmailIgnoreCase(normal.email).orElseThrow();
        assertEquals(Role.USER, unchanged.role);
        assertTrue(unchanged.enabled);
        assertEquals(Role.ADMIN, users.findByEmailIgnoreCase(configuredAdminEmail).orElseThrow().role);
        assertEquals(2, users.count());
    }

    private void provision() {
        provisioner.run(new DefaultApplicationArguments(new String[0]));
    }
}
