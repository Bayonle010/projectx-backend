package com.project_x.verification.otp.service;

import com.project_x.core.response.ApiResponse;
import com.project_x.user.enums.UserType;
import com.project_x.verification.otp.dto.response.OtpValidationResponse;
import com.project_x.verification.otp.enums.OtpEvent;
import org.springframework.http.ResponseEntity;

public interface OtpService {
    boolean handleGenerateOtp(String otpMedium, OtpEvent otpEvent, long expirationTimeInSeconds, String emailSubject, UserType userType, String emailTemplate);
    OtpValidationResponse validateOtp(String otp, String otpMedium, OtpEvent event);
}
