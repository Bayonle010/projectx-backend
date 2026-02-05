package com.project_x.authentication.service;

import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<ApiResponse> registerUser(AuthRequest request);
}
