package com.project_x.authentication.customauth.service;

import com.project_x.authentication.customauth.dto.request.LoginRequest;
import com.project_x.authentication.customauth.dto.request.RefreshTokenRequest;
import com.project_x.authentication.customauth.dto.request.RegistrationRequest;
import com.project_x.core.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ApiResponse> registerUser(RegistrationRequest request);
    ResponseEntity<ApiResponse> authenticateUser(LoginRequest request);
    ResponseEntity<ApiResponse> refreshToken(RefreshTokenRequest request);
    void logout(HttpServletRequest request);
}
