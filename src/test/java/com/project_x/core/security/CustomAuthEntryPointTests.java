package com.project_x.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAuthEntryPointTests {

    @Test
    void returnsUnauthorizedForAuthenticationFailure() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET",
                "/api/v1/listings"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        CustomAuthEntryPoint entryPoint =
                new CustomAuthEntryPoint(new ObjectMapper());
        entryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Authentication required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(
                "Authentication is required. Please log in."
        );
    }
}
