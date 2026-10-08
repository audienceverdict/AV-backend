package com.example.moviebooking;

import com.example.moviebooking.auth.entity.*;
import com.example.moviebooking.auth.repository.*;
import com.example.moviebooking.auth.provider.EmailProvider;
import com.example.moviebooking.auth.security.JwtService;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({ "dev", "test" })
class AuthIntegrationTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    UserRepository users;
    @Autowired
    OtpVerificationRepository otps;
    @Autowired
    JwtService jwt;
    @Value("${app.admin.email:}")
    String configuredAdminEmail;
    @MockitoBean
    EmailProvider email;
    final Map<String, String> codes = new ConcurrentHashMap<>();

    @BeforeEach
    void setup() {
        otps.deleteAll();
        users.deleteAll();
        codes.clear();
        doAnswer(i -> {
            String to = i.getArgument(0);
            String html = i.getArgument(3);
            var match = java.util.regex.Pattern.compile(">([0-9]{6})</strong>").matcher(html);
            if (match.find())
                codes.put(to, match.group(1));
            return null;
        }).when(email).sendHtml(anyString(), anyString(), anyString(), anyString(), any());
    }

    String body(Object value) throws Exception {
        return json.writeValueAsString(value);
    }

    void request(String address, int status) throws Exception {
        mvc.perform(post("/api/v1/auth/email-otp/request").contentType("application/json")
                .content(body(Map.of("email", address)))).andExpect(status().is(status));
    }

    JsonNode verify(String address, String code, int status) throws Exception {
        var result = mvc
                .perform(post("/api/v1/auth/email-otp/verify").contentType("application/json")
                        .content(body(Map.of("email", address, "otp", code))))
                .andExpect(status().is(status)).andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }

    JsonNode login(String address, String mobile) throws Exception {
        request(address, 200);
        var result = verify(address, codes.get(address), 200);
        if (result.path("registrationRequired").asBoolean())
            return register(address, "John", mobile, 200);
        return result;
    }

    JsonNode register(String address, String name, String mobile, int status) throws Exception {
        var result = mvc
                .perform(post("/api/v1/auth/email-otp/register").contentType("application/json")
                        .content(body(Map.of("email", address, "name", name, "mobile", mobile))))
                .andExpect(status().is(status)).andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }

    String token(JsonNode auth) {
        return "Bearer " + auth.get("accessToken").asText();
    }


    void newAccountRequiresVerifiedEmailAndCompleteProfile() throws Exception {
        request("john@example.com", 200);
        var pending = verify("john@example.com", codes.get("john@example.com"), 200);
        assertTrue(pending.path("registrationRequired").asBoolean());
        assertTrue(pending.get("accessToken").isNull());
        register("john@example.com", "John", "bad", 400);
        var auth = register("john@example.com", "John", "9876543210", 200);
        assertEquals("john@example.com", auth.at("/user/email").asText());
        assertEquals("John", auth.at("/user/name").asText());
        assertEquals("+919876543210", auth.at("/user/mobile").asText());
        assertEquals(1, users.count());
        verify("john@example.com", codes.get("john@example.com"), 400);
        var again = login("john@example.com", "9876543210");
        assertEquals(auth.at("/user/id"), again.at("/user/id"));
    }

    @Test
    void otpHashDeliveryExpiryAndLimits() throws Exception {
        request("john@example.com", 200);
        String code = codes.get("john@example.com");
        assertTrue(code.matches("[0-9]{6}"));
        var record = otps.findFirstByMobileAndChannelOrderByIdDesc("john@example.com", "EMAIL").orElseThrow();
        assertNotEquals(code, record.otpHash);
        assertTrue(
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(code, record.otpHash));
        request("john@example.com", 429);
        String wrong = code.equals("000000") ? "000001" : "000000";
        for (int i = 0; i < 5; i++)
            verify("john@example.com", wrong, 400);
        verify("john@example.com", code, 429);
        assertEquals(5, otps.findById(record.id).orElseThrow().attemptCount);
        record = otps.findById(record.id).orElseThrow();
        record.createdAt = Instant.now().minusSeconds(31);
        otps.save(record);
        request("john@example.com", 200);
        record = otps.findFirstByMobileAndChannelOrderByIdDesc("john@example.com", "EMAIL").orElseThrow();
        record.expiresAt = Instant.now().minusSeconds(1);
        otps.save(record);
        verify("john@example.com", codes.get("john@example.com"), 400);
    }

    @Test
    void authorizationUsesCurrentDatabaseRoleAndStatus() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        var auth = login("john@example.com", "9876543210");
        mvc.perform(get("/api/v1/admin/users").header("Authorization", token(auth))).andExpect(status().isForbidden());
        var user = users.findByEmailIgnoreCase("john@example.com").orElseThrow();
        user.role = Role.ADMIN;
        users.save(user);
        mvc.perform(get("/api/v1/admin/users").header("Authorization", token(auth))).andExpect(status().isOk());
        var other = login("jane@example.com", "9876543211");
        String id = other.at("/user/id").asText();
        mvc.perform(patch("/api/v1/admin/users/" + id + "/role").header("Authorization", token(auth))
                .contentType("application/json").content("{\"role\":\"USER\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/admin/users/" + id + "/status").header("Authorization", token(auth))
                .contentType("application/json").content("{\"enabled\":false}")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", token(other))).andExpect(status().isUnauthorized());
    }

    @Test
    void configuredAdminEmailIsPromotedAfterOtpAndOtherAdminEmailIsRejected() throws Exception {
        String configuredAdmin = configuredAdminEmail;
        User configuredUser = new User();
        configuredUser.name = "Configured Admin";
        configuredUser.mobile = "+919876543210";
        configuredUser.email = configuredAdmin;
        configuredUser.role = Role.USER;
        users.saveAndFlush(configuredUser);

        request(configuredAdmin, 200);
        var result = verify(configuredAdmin.toUpperCase(Locale.ROOT), codes.get(configuredAdmin), 200);

        assertFalse(result.path("registrationRequired").asBoolean());
        assertEquals("ADMIN", jwt.validate(result.path("accessToken").asText()).getClaimAsString("role"));
        assertEquals(Role.ADMIN, users.findByEmailIgnoreCase(configuredAdmin).orElseThrow().role);

        User otherAdmin = new User();
        otherAdmin.name = "Other Admin";
        otherAdmin.mobile = "+919876543211";
        otherAdmin.email = "other-admin@example.com";
        otherAdmin.role = Role.ADMIN;
        users.saveAndFlush(otherAdmin);
        request(otherAdmin.email, 403);
        verify(otherAdmin.email, "123456", 403);
    }

    @Test
    void validationAndUniqueEmail() throws Exception {
        request("bad", 400);
        mvc.perform(post("/api/v1/auth/email-otp/request").contentType("application/json").content("{invalid"))
                .andExpect(status().isBadRequest());
        var first = login("john@example.com", "9876543210");
        var second = login("jane@example.com", "9876543211");
        String profile = "{\"name\":\"John\",\"email\":\"jane@example.com\"}";
        mvc.perform(put("/api/v1/auth/me").header("Authorization", token(first)).contentType("application/json")
                .content(profile)).andExpect(status().isBadRequest());
        for (String field : List.of("role", "mobile", "enabled", "id", "createdAt"))
            mvc.perform(put("/api/v1/auth/me").header("Authorization", token(first)).contentType("application/json")
                    .content(body(Map.of("name", "John", field, "ADMIN", "email", "john@example.com"))))
                    .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/auth/me").header("Authorization", token(first)).contentType("application/json")
                .content(body(Map.of("name", " ", "email", "bad")))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void corsAndExpiredToken() throws Exception {
        mvc.perform(options("/api/v1/auth/me").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "authorization")).andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        var auth = login("john@example.com", "9876543210");
        var user = users.findById(auth.at("/user/id").asText()).orElseThrow();
        var shortJwt = new JwtService("test-only-secret-with-at-least-thirty-two-bytes", 1);
        String expired = shortJwt.generate(user);
        Thread.sleep(20);
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }
}
