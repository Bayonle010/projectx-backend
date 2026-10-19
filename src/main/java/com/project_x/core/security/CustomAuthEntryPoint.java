package com.project_x.core.security;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = authException instanceof BadCredentialsException
                && StringUtils.hasText(authException.getMessage())
                ? authException.getMessage()
                : "Authentication is required. Please log in.";

        ApiResponse apiResponse = ResponseUtil.error(
                HttpServletResponse.SC_UNAUTHORIZED,
                message,
                "Unauthorized",
                null
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

}
