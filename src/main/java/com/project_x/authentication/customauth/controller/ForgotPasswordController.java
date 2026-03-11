package com.project_x.authentication.customauth.controller;

import com.project_x.authentication.customauth.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.customauth.dto.request.ResetPasswordRequest;
import com.project_x.authentication.customauth.dto.request.VerifyPasswordOtpRequest;
import com.project_x.authentication.customauth.service.ForgotPasswordService;
import com.project_x.core.response.ApiResponse;
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
    public ResponseEntity<ApiResponse> verifyForgotPasswordOtp(@RequestBody @Valid VerifyPasswordOtpRequest request){
        return forgotPasswordService.handlePasswordOtpVerification(request);
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request){
        return forgotPasswordService.resetPassword(request);
    }
}
