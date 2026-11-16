package com.project_x.authentication.socialauth.handler;

import com.project_x.authentication.customauth.token.service.TokenService;
import com.project_x.core.security.JwtUtil;
import com.project_x.user.entity.User;
import com.project_x.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OAuth2AuthenticationSuccessHandlerTests {

    @Test
    void persistsRefreshTokenBeforeRedirectingSuccessfulSocialLogin() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenService tokenService = mock(TokenService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);
        OAuth2User oauth2User = mock(OAuth2User.class);
        User user = User.builder()
                .email("user@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.<String>getAttribute("email"))
                .thenReturn(" User@Example.com ");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessTokenForUser(user))
                .thenReturn("access-token");
        when(jwtUtil.generateRefreshTokenForUser(user))
                .thenReturn("refresh-token");

        OAuth2AuthenticationSuccessHandler handler =
                new OAuth2AuthenticationSuccessHandler(
                        userRepository,
                        jwtUtil,
                        tokenService,
                        "https://app.example.com"
                );

        handler.onAuthenticationSuccess(request, response, authentication);

        InOrder completionOrder = inOrder(tokenService, response);
        completionOrder.verify(tokenService)
                .saveRefreshToken(user, "refresh-token");
        completionOrder.verify(response).sendRedirect(anyString());
    }

    @Test
    void redirectsWithExistingErrorFormatWhenRefreshTokenPersistenceFails() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenService tokenService = mock(TokenService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Authentication authentication = mock(Authentication.class);
        OAuth2User oauth2User = mock(OAuth2User.class);
        User user = User.builder()
                .email("user@example.com")
                .build();

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.<String>getAttribute("email"))
                .thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessTokenForUser(user))
                .thenReturn("access-token");
        when(jwtUtil.generateRefreshTokenForUser(user))
                .thenReturn("refresh-token");
        doThrow(new IllegalStateException("database unavailable"))
                .when(tokenService)
                .saveRefreshToken(user, "refresh-token");

        OAuth2AuthenticationSuccessHandler handler =
                new OAuth2AuthenticationSuccessHandler(
                        userRepository,
                        jwtUtil,
                        tokenService,
                        "https://app.example.com"
                );

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectUrl = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectUrl.capture());
        assertTrue(redirectUrl.getValue().contains("oauth_error=true"));
        assertTrue(redirectUrl.getValue().contains(
                "message=Unable%20to%20complete%20social%20login.%20Please%20try%20again."
        ));
    }
}
