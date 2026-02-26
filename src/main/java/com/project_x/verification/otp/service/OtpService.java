package com.project_x.verification.otp.service;

import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface OtpService {
    ResponseEntity<ApiResponse> handleGenerateOtp(String otpMedium, OtpEvent otpEvent, long expirationTimeInSeconds, String emailSubject, UserType userType, String emailTemplate);
}
