package com.project_x.core.security;

import com.project_x.core.exception.InvalidCredentialException;
import com.project_x.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFilterTests {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsCleanUnauthorizedResponseWhenTokenCannotBeDecoded()
            throws Exception {
        JwtFilter filter = filterWithRejectingDecoder();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/listings");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains(
                "Your session is invalid or has expired. Please log in again."
        );
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void continuesAsGuestWhenAuthorizationHeaderIsMissing()
            throws Exception {
        JwtFilter filter = filterWithRejectingDecoder();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/coverage/states");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(chain.getResponse()).isSameAs(response);
    }

    private JwtFilter filterWithRejectingDecoder() {
        UserService userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class<?>[]{UserService.class},
                (proxy, method, arguments) -> null
        );
        JwtUtil jwtUtil = new JwtUtil(null, null) {
            @Override
            public Jwt decodeJwt(String token) {
                throw new InvalidCredentialException("Invalid or expired token");
            }
        };

        return new JwtFilter(
                userService,
                jwtUtil,
                new CustomUserDetailsServiceImpl(userService),
                new CustomAuthEntryPoint(new ObjectMapper())
        );
    }
}
