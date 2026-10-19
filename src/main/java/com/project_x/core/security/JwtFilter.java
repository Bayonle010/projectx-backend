package com.project_x.core.security;

import com.project_x.core.exception.InvalidCredentialException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsServiceImpl userDetailsService;
    private final CustomAuthEntryPoint authenticationEntryPoint;

    public JwtFilter(
            UserService userService,
            JwtUtil jwtUtil,
            CustomUserDetailsServiceImpl userDetailsService,
            CustomAuthEntryPoint authenticationEntryPoint
    ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        try {
            authenticateRequest(request, jwtToken);
        } catch (
                InvalidCredentialException
                | ResourceNotFoundException
                | AuthenticationException exception
        ) {
            SecurityContextHolder.clearContext();
            log.warn(
                    "JWT authentication rejected for {} {}: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    exception.getMessage()
            );
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException(
                            "Your session is invalid or has expired. Please log in again.",
                            exception
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateRequest(HttpServletRequest request, String jwtToken)
    {
        Jwt jwt = jwtUtil.decodeJwt(jwtToken);
        String userEmail = jwt.getSubject();

        if (userEmail == null || userEmail.isBlank()) {
            throw new InvalidCredentialException("Token does not identify a user");
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userService.findUserByEmail(userEmail);

            Number tokenSessionVersion = jwt.getClaim("sessionVersion");

            if (tokenSessionVersion == null
                    || tokenSessionVersion.longValue()
                    != user.getSessionVersion()) {
                throw new InvalidCredentialException("Token session is no longer valid");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            AuthenticationIdentity authenticationIdentity = jwtUtil.initialiseAuthenticationIdentity(jwt);
            request.setAttribute("AUTH_IDENTITY", authenticationIdentity);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui/") || uri.equals("/v2/api-docs")
                || uri.startsWith("/v3/api-docs/") || uri.startsWith("/swagger-resources/")
                || uri.startsWith("/configuration/ui") || uri.startsWith("/configuration/security")
                || uri.startsWith("/webjars/") || uri.equals("/swagger-ui.html")
                || uri.startsWith("/api/v1/auth/") || uri.startsWith("/login/")
                || uri.startsWith("/oauth2/") || uri.startsWith("/ws/");
    }
}
