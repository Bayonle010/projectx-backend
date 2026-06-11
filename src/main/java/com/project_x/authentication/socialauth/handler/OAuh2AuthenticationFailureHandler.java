package com.project_x.authentication.socialauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

public class OAuh2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final String appUrl;

    public OAuh2AuthenticationFailureHandler(@Value("${app.base-url}") String appUrl) {
        this.appUrl = appUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = "Social authentication failed";

        String redirectUrl = UriComponentsBuilder
                .fromUriString(appUrl)
                .path("/login")
                .queryParam("oauth_error", true)
                .queryParam("message", errorMessage)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
