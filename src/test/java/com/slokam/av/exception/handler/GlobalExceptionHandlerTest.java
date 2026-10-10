package com.slokam.av.exception.handler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void unexpectedFailuresHaveGenericResponse() {
        var response = handler.unexpected(new IllegalStateException("secret credential"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).doesNotContain("secret credential");
    }

    @Test
    void frameworkStatusIsPreserved() {
        var response = handler.unexpected(new ResponseStatusException(HttpStatus.NOT_FOUND, "private detail"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().toString()).doesNotContain("private detail");
    }

    @Test
    void accessDenialRemainsForbidden() {
        assertThat(handler.forbidden(request).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
