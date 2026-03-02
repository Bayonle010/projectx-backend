package com.project_x.authentication.controller;

import com.project_x.authentication.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.dto.request.VerifyPasswordOtpRequest;
import com.project_x.authentication.service.ForgotPasswordService;
import com.project_x.core.response.ApiResponse;
import com.project_x.verification.otp.dto.request.VerifyOtpRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/forgot-password")
public class ForgotPasswordController {
    private final ForgotPasswordService forgotPasswordService;

    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }


    @PostMapping("/otp")
    public ResponseEntity<ApiResponse> sendForgotPasswordOtp(@RequestBody @Valid ForgotPasswordOtpRequest request){
        return forgotPasswordService.handleSendForgotPasswordOtp(request);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verifyForgotPasswordOtp(VerifyPasswordOtpRequest request){
        return forgotPasswordService.handlePasswordOtpVerification(request);
    }
}
