package com.project_x.core.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAuthEntryPointTests {

    @Test
    void returnsUserFriendlyMessageForInvalidToken() throws Exception {
        CustomAuthEntryPoint entryPoint = new CustomAuthEntryPoint(
                new ObjectMapper()
        );
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException(
                        "Your session is invalid or has expired. Please log in again."
                )
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(
                "Your session is invalid or has expired. Please log in again."
        );
    }
}
