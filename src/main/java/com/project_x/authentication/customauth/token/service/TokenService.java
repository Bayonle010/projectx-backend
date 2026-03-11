package com.project_x.authentication.customauth.token.service;

import com.project_x.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface TokenService {
    void saveRefreshToken(User user, String refreshToken);

    void revokeAllUserTokens(User user);

    boolean isRefreshTokenValid(String rawToken, User user);

    void deleteExpiredTokens();


}
