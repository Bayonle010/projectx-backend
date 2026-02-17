package com.project_x.authentication.service;

import com.project_x.authentication.dto.request.LoginRequest;
import com.project_x.authentication.dto.request.RefreshTokenRequest;
import com.project_x.authentication.dto.request.RegistrationRequest;
import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ApiResponse> registerUser(RegistrationRequest request);
    ResponseEntity<ApiResponse> authenticateUser(LoginRequest request);
    ResponseEntity<ApiResponse> refreshToken(RefreshTokenRequest request);
}
