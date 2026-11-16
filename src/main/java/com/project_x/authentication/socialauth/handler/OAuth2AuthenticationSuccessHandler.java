package com.project_x.authentication.socialauth.handler;

import com.project_x.core.exception.BadRequestException;
import com.project_x.authentication.customauth.token.service.TokenService;
import com.project_x.core.security.JwtUtil;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(
            OAuth2AuthenticationSuccessHandler.class
    );

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final String appUrl;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,
                                              JwtUtil jwtUtil,
                                              TokenService tokenService,
                                              @Value("${app.base-url}") String appUrl) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
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
            redirectWithError(response, "Unable to get email from social account");
            return;
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElse(null);


        if (user == null) {
            redirectWithError(response, "No account found for this social login");
            return;
        }


        String accessToken;
        String refreshToken;
        try {
            accessToken = jwtUtil.generateAccessTokenForUser(user);
            refreshToken = jwtUtil.generateRefreshTokenForUser(user);
            tokenService.saveRefreshToken(user, refreshToken);
        } catch (RuntimeException exception) {
            log.error("Unable to complete social login for userId={}", user.getId(), exception);
            redirectWithError(response, "Unable to complete social login. Please try again.");
            return;
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(appUrl)
                .path("/login")
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void redirectWithError(HttpServletResponse response, String message) throws IOException {
        String redirectUrl = UriComponentsBuilder
                .fromUriString(appUrl)
                .path("/login")
                .queryParam("oauth_error", true)
                .queryParam("message", message)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

}
