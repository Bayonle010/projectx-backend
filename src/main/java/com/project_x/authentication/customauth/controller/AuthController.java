package com.project_x.authentication.customauth.controller;

import com.project_x.authentication.customauth.dto.request.LoginRequest;
import com.project_x.authentication.customauth.dto.request.RefreshTokenRequest;
import com.project_x.authentication.customauth.dto.request.RegistrationRequest;
import com.project_x.authentication.customauth.service.AuthService;
import com.project_x.core.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private  final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser( @Valid @RequestBody RegistrationRequest request){
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticate( @Valid @RequestBody LoginRequest request){
        return authService.authenticateUser(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> authenticate( @Valid @RequestBody RefreshTokenRequest request){
        return authService.refreshToken(request);
    }
}
