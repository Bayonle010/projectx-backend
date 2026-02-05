package com.project_x.core.security;

import com.project_x.core.security.model.AuthenticationIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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

            // Load user from DB to read sessionVersion
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Long tokenSessionVersion = jwt.getClaim("sessionVersion");
            if (tokenSessionVersion == null ||
                    tokenSessionVersion != user.getSessionVersion()) {
                // user has logged out or session invalidated
                throw new IllegalArgumentException("Invalid or expired session");
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
}
