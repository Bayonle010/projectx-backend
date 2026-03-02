package com.project_x.authentication.service;

import com.project_x.authentication.dto.request.ForgotPasswordOtpRequest;
import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface ForgotPasswordService {
    ResponseEntity<ApiResponse> handleSendForgotPasswordOtp(ForgotPasswordOtpRequest request);
}
