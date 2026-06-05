package com.project_x.core.security;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsServiceImpl userDetailsService;

    public JwtFilter(UserService userService, JwtUtil jwtUtil, CustomUserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>Authorization Begins<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        final String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwtToken = authHeader.substring(7);

        // Decode with RSA via Spring JwtDecoder
        Jwt jwt = jwtUtil.decodeJwt(jwtToken);
        String userEmail = jwt.getSubject();

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userService.findUserByEmail(userEmail);

            Long tokenSessionVersion = jwt.getClaim("sessionVersion");

            if (tokenSessionVersion == null || tokenSessionVersion != user.getSessionVersion()){
                // user has logged out or session is invalidated
                throw new IllegalArgumentException("invalid or Expired session");
            }


            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // decodeJwt already validates exp & signature; here we can just trust it.
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

        filterChain.doFilter(request, response);
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
