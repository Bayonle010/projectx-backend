package com.project_x.authentication.service.impl;

import com.project_x.authentication.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.service.ForgotPasswordService;
import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public class ForgotPasswordImpl implements ForgotPasswordService {
    @Override
    public ResponseEntity<ApiResponse> handleSendForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        return null;
    }
}
