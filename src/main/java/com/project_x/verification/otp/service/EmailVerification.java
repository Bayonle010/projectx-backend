package com.project_x.verification.otp.service;

import com.project_x.core.response.ApiResponse;
import com.project_x.verification.otp.dto.request.SignUpOtpRequest;
import com.project_x.verification.otp.dto.request.VerifyOtpRequest;
import com.project_x.verification.otp.entity.Otp;
import org.springframework.http.ResponseEntity;

public interface EmailVerification {
    ResponseEntity<ApiResponse> generateSignUpOtp(SignUpOtpRequest signUpOtpRequest);
    ResponseEntity<ApiResponse> verifyOtp(VerifyOtpRequest verifyOtpRequest);
    ResponseEntity<ApiResponse> handleVerifySignUpOtp(Otp otp);
}
