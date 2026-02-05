package com.project_x.authentication.service.impl;

import com.project_x.authentication.dto.request.RegistrationRequest;
import com.project_x.authentication.service.AuthService;
import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public ResponseEntity<ApiResponse> registerUser(RegistrationRequest request) {
        return null;
    }
}
