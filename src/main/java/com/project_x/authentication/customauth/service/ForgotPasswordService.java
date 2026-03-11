package com.project_x.authentication.customauth.service;

import com.project_x.authentication.customauth.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.customauth.dto.request.ResetPasswordRequest;
import com.project_x.authentication.customauth.dto.request.VerifyPasswordOtpRequest;
import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ForgotPasswordService {
    ResponseEntity<ApiResponse> handleSendForgotPasswordOtp(ForgotPasswordOtpRequest request);
    ResponseEntity<ApiResponse> handlePasswordOtpVerification(VerifyPasswordOtpRequest request);
    ResponseEntity<ApiResponse> resetPassword(ResetPasswordRequest request);

}
