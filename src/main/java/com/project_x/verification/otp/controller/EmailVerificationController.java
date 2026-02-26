package com.project_x.verification.otp.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.verification.otp.dto.request.SignUpOtpRequest;
import com.project_x.verification.otp.dto.request.VerifyOtpRequest;
import com.project_x.verification.otp.service.EmailVerification;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class EmailVerificationController {
    private final EmailVerification emailVerification;

    public EmailVerificationController(EmailVerification emailVerification) {
        this.emailVerification = emailVerification;
    }

    @PostMapping("/generate/otp")
    public ResponseEntity<ApiResponse> generateOtp(@RequestBody @Valid SignUpOtpRequest signUpOtpRequest){
        return emailVerification.generateSignUpOtp(signUpOtpRequest);
    }


    @PostMapping("/verify/otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody @Valid VerifyOtpRequest verifyOtpRequest){
        return emailVerification.verifyOtp(verifyOtpRequest);
    }
}
