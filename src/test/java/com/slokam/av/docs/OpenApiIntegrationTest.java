package com.slokam.av.docs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slokam.av.config.*;
import com.slokam.av.controller.*;
import com.slokam.av.filter.JwtAuthenticationFilter;
import com.slokam.av.repository.NotificationRepository;
import com.slokam.av.security.*;
import com.slokam.av.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OpenApiIntegrationTest.Config.class, properties = {
        "app.cors-origins=http://localhost:3000", "spring.flyway.enabled=false"})
@ActiveProfiles("openapi-test")
@AutoConfigureMockMvc
class OpenApiIntegrationTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @Import({OpenApiConfig.class, SecurityConfig.class, JwtAuthenticationFilter.class,
            AuthController.class, OtpController.class, AdminUserController.class, MovieController.class,
            MovieCreditController.class, MovieMediaController.class, PeopleController.class,
            TheatreController.class, ShowController.class, BookingController.class,
            PaymentController.class, ReviewController.class, WaitingListController.class,
            NotificationController.class})
    static class Config {}
    @MockitoBean ConfiguredOtpService configuredOtp;
    @MockitoBean OtpChannelConfig otpConfig;
    @MockitoBean AuthService auth;
    @MockitoBean MovieService movies;
    @MockitoBean MovieCreditService credits;
    @MockitoBean MovieMediaService media;
    @MockitoBean PeopleService people;
    @MockitoBean TheatreService theatres;
    @MockitoBean LayoutCompatibilityService layouts;
    @MockitoBean ShowService shows;
    @MockitoBean BookingService bookings;
    @MockitoBean PaymentService payments;
    @MockitoBean ReviewService reviews;
    @MockitoBean WaitingListService waiting;
    @MockitoBean NotificationService notifications;
    @MockitoBean NotificationRepository notificationRepository;
    @MockitoBean JwtService jwt;
    @MockitoBean CustomUserDetailsService users;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test void completeApiSpecificationIsPublicAndDocumentsAccessAndModels() throws Exception {
        var response = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        JsonNode api = mapper.readTree(response.getResponse().getContentAsString());
        JsonNode paths = api.path("paths");
        assertTrue(paths.size() > 45, "Every controller must be included");
        int[] operations = {0};
        paths.fields().forEachRemaining(path -> path.getValue().fields().forEachRemaining(operation -> {
            var value = operation.getValue();
            assertFalse(value.path("summary").asText().isBlank(), path.getKey());
            assertTrue(value.path("description").asText().contains("Access:"), path.getKey());
            assertTrue(value.path("responses").has("400"), path.getKey());
            operations[0]++;
        }));
        assertEquals(92, operations[0]);
        assertEquals(0, paths.path("/api/v1/movies").path("get").path("security").size());
        assertEquals("ADMIN", paths.path("/api/v1/movies").path("post").path("x-required-role").asText());
        assertEquals("AUTHENTICATED", paths.path("/api/v1/bookings/me").path("get").path("x-required-role").asText());
        assertEquals("AUTHENTICATED", paths.path("/api/v1/bookings/{id}/ticket-qr").path("get").path("x-required-role").asText());
        assertEquals("ADMIN", paths.path("/api/v1/bookings/admin/ticket-scan").path("post").path("x-required-role").asText());
        assertEquals("PUBLIC", paths.path("/api/v1/payments/booking/{bookingId}").path("get").path("x-required-role").asText());
        assertTrue(api.path("components").path("schemas").has("ApiErrors"));
        assertTrue(api.path("components").path("schemas").has("MovieRequest"));
        assertTrue(api.path("components").path("schemas").has("EmailOtpResult"));
        assertTrue(paths.path("/api/v1/movies/{movieId}/media").path("post").path("responses").has("201"));
        assertTrue(paths.path("/api/v1/movies/{movieId}/media/{mediaId}").path("delete").path("responses").has("204"));
    }

    @Test void registrationEndpointsArePublicEvenWithStaleAuthorization() throws Exception {
        String[] paths={"registration/request","registration/verify","registration/email/request","registration/email/verify"};
        String[] bodies={"{\"destination\":\"9000000001\"}",
            "{\"destination\":\"9000000001\",\"otp\":\"123456\"}",
            "{\"registrationToken\":\"proof\",\"email\":\"a@example.test\"}",
            "{\"registrationToken\":\"proof\",\"email\":\"a@example.test\",\"otp\":\"123456\"}"};
        for(int i=0;i<paths.length;i++) mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/otp/"+paths[i])
            .header("Authorization","Bearer expired").contentType("application/json").content(bodies[i])).andExpect(status().isOk());
    }

    @Test void swaggerUiAndConfigurationArePublicAndBusinessApiStaysProtected() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
        mvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/bookings")).andExpect(status().isUnauthorized());
    }
}
