package com.project_x.core.security;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider;

    public CustomAuthEntryPoint(
            ObjectMapper objectMapper,
            ObjectProvider<RequestMappingHandlerMapping> handlerMappingProvider
    ) {
        this.objectMapper = objectMapper;
        this.handlerMappingProvider = handlerMappingProvider;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");

        if (isUnknownApiEndpoint(request)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            ApiResponse apiResponse = ResponseUtil.error(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The requested endpoint does not exist",
                    "Resource not found",
                    null
            );

            objectMapper.writeValue(response.getWriter(), apiResponse);
            return;
        }

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

    private boolean isUnknownApiEndpoint(HttpServletRequest request) {
        if (!request.getRequestURI().startsWith("/api/")) {
            return false;
        }

        RequestMappingHandlerMapping handlerMapping = handlerMappingProvider.getIfAvailable();

        if (handlerMapping == null) {
            return false;
        }

        try {
            return handlerMapping.getHandler(request) == null;
        } catch (Exception ignored) {
            // A mapping error such as an unsupported HTTP method is not the
            // same as an unknown endpoint and should be handled separately.
            return false;
        }
    }

}
