package com.project_x.verification.otp.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.verification.otp.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OtpServiceImpl implements OtpService {
    @Override
    public ResponseEntity<ApiResponse> handleGenerateOtp(String otpMedium, OtpEvent otpEvent, long expirationTimeInSeconds, String emailSubject, UserType userType, String emailTemplate) {
        return null;
    }
}
