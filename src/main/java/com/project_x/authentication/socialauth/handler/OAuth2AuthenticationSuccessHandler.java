package com.project_x.authentication.socialauth.handler;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.JwtUtil;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final String appUrl;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,
                                              JwtUtil jwtUtil,
                                              @Value("${app.base-url}") String appUrl) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.appUrl = appUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Authenticated social user email not found");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BadRequestException("Authenticated user not found in database"));

        String accessToken = jwtUtil.generateAccessTokenForUser(user);


        String redirectUrl = UriComponentsBuilder
                .fromUriString(appUrl)
                .path("/auth/social-success")
                .queryParam("token", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

}
