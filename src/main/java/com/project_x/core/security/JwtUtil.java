package com.project_x.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final long ACCESS_TOKEN_DURATION_FOR_USER_IN_SEC = 24 * 60L *60L *360L * ;  // set to 15 minutes

    private static final long ACCESS_TOKEN_DURATION_FOR_ADMIN_IN_SEC = 10 * 60L; // set to 10 minutes

    private static final long REFRESH_TOKEN_DURATION_FOR_USER_IN_SEC =   24 * 60 * 60L * 360L; // set to 24 hours

    private static final long REFRESH_TOKEN_DURATION_FOR_ADMIN_IN_SEC = 1 * 60 * 60L; // set to 1 hour

    private static final String ISSUER = "bayfi";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }


    /* ------------------------------------------------------------------
     * ACCESS TOKEN
     * ------------------------------------------------------------------ */

    public String generateAccessTokenForUser(User user){
        return generateAccessToken(user, ACCESS_TOKEN_DURATION_FOR_USER_IN_SEC);
    }

    public String generateAccessTokenForAdmin(User user) {
        return generateAccessToken(user, ACCESS_TOKEN_DURATION_FOR_ADMIN_IN_SEC);
    }

    public String generateAccessToken(Authentication auth, String email) {
        // If you prefer building from Authentication instead of User
        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            roles.add("ROLE_USER");
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_DURATION_FOR_USER_IN_SEC))
                .subject(email)
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateAccessToken(User user, long durationSeconds) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            // default role depending on userType if you prefer
            roles.add("ROLE_USER");
        }

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(durationSeconds))
                .subject(user.getEmail())
                .claim("roles", roles)
                .claim("firstName", user.getFirstname())
                .claim("lastName", user.getLastname())
                .claim("id", user.getId().toString())
                .claim("userType", user.getUserType())
                .claim("username", user.getUsername())
                .claim("tokenType", "ACCESS")
                .claim("sessionVersion", user.getSessionVersion())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


    /* ------------------------------------------------------------------
     * REFRESH TOKENS
     * ------------------------------------------------------------------ */

    public String generateRefreshTokenForUser(User user) {
        return generateRefreshToken(user, REFRESH_TOKEN_DURATION_FOR_USER_IN_SEC);
    }

    public String generateRefreshTokenForAdmin(User user) {
        return generateRefreshToken(user, REFRESH_TOKEN_DURATION_FOR_ADMIN_IN_SEC);
    }

    private String generateRefreshToken(User user, long durationSeconds) {
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toList());

        if (roles.isEmpty()) {
            roles.add("ROLE_USER");
        }

        String tokenId = UUID.randomUUID().toString();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(durationSeconds))
                .subject(user.getEmail())
                .claim("roles", roles)
                .claim("firstName", user.getFirstname())
                .claim("lastName", user.getLastname())
                .claim("id", user.getId().toString())
                .claim("userType", user.getUserType())
                .claim("username", user.getUsername())
                .claim("tokenId", tokenId)
                .claim("tokenType", "REFRESH")
                .claim("sessionVersion", user.getSessionVersion())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }


    /* ------------------------------------------------------------------
     * DECODING & UTILITIES
     * ------------------------------------------------------------------ */

    public Jwt decodeJwt(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException e) {
            log.error("Failed to decode JWT token", e);
            throw new InvalidCredentialException("Invalid JWT token");
        }
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public AuthenticationIdentity initialiseAuthenticationIdentity(Jwt jwt) {
        try {
            return AuthenticationIdentity.builder()
                    .id(jwt.getClaim("id"))
                    .firstName(jwt.getClaim("firstName"))
                    .lastName(jwt.getClaim("lastName"))
                    .email(jwt.getSubject())
                    .userType(jwt.getClaim("userType"))
                    .build();
        } catch (Exception e) {
            throw new InvalidCredentialException("Failed to build authentication identity from JWT");
        }
    }
}
