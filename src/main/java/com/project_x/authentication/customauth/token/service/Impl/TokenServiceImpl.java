package com.project_x.authentication.customauth.token.service.Impl;

import com.project_x.authentication.customauth.token.Token;
import com.project_x.authentication.customauth.token.TokenRepository;
import com.project_x.authentication.customauth.token.TokenType;
import com.project_x.authentication.customauth.token.service.TokenService;
import com.project_x.core.exception.InvalidCredentialException;
import com.project_x.core.security.JwtUtil;
import com.project_x.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service("TokenService")
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public void saveRefreshToken(User user, String refreshToken) {
        Jwt jwt = jwtUtil.decodeJwt(refreshToken);

        String tokenId = jwt.getClaim("tokenId");
        if (tokenId == null) {
            throw new InvalidCredentialException("Invalid refresh token: tokenId missing");
        }

        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();

        Token token = Token.builder()
                .user(user)
                .token(tokenId)  // store tokenId, not the whole JWT
                .tokenType(TokenType.BEARER)
                .createdAt(issuedAt != null ? issuedAt : Instant.now())
                .expiresAt(expiresAt)
                .revoked(false)
                .expired(false)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        List<Token> validTokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (validTokens.isEmpty()) {
            return;
        }

        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });

        tokenRepository.saveAll(validTokens);
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken, User user) {

        Jwt jwt = jwtUtil.decodeJwt(refreshToken);

        String tokenId = jwt.getClaim("tokenId");
        if (tokenId == null) {
            throw new InvalidCredentialException("Invalid refresh token: tokenId missing");
        }

        Token token = tokenRepository.findByToken(tokenId)
                .orElseThrow(() -> new InvalidCredentialException("token not found"));

        if (token.isExpired() || token.isRevoked()) {
            throw new InvalidCredentialException("token is no longer valid");
        }

        // Optionally ensure token subject matches the user
        String subjectEmail = jwt.getSubject();
        if (subjectEmail == null || !subjectEmail.equals(user.getEmail())) {
            throw new InvalidCredentialException("refresh token does not belong to this user");
        }

        return true;
    }

    @Scheduled(fixedRate = 1000L * 60 * 60 * 24 * 365) // every 365 days
    @Override
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        log.info("Cleaning up expired Tokens at {}", now);

        tokenRepository.deleteAllExpiredSince(now);

        log.info("Expired tokens cleanup completed at {}", now);
    }

}
