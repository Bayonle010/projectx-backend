package com.project_x.authentication.service.impl;

import com.project_x.authentication.dto.request.ForgotPasswordOtpRequest;
import com.project_x.authentication.dto.request.VerifyPasswordOtpRequest;
import com.project_x.authentication.service.ForgotPasswordService;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.user.enums.UserType;
import com.project_x.user.service.UserService;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordImpl implements ForgotPasswordService {

    private final OtpService otpService;

    public ForgotPasswordImpl(OtpService otpService) {
        this.otpService = otpService;
    }


    @Override
    public ResponseEntity<ApiResponse> handleSendForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        String formattedEmail = request.email().toLowerCase().trim();


        boolean generateOtp = otpService.handleGenerateOtp(
                formattedEmail, OtpEvent.FORGOT_PASSWORD, 300L, "PASSWORD RESET OTP",
                UserType.USER, "forgot password otp :"
        );

        if (!generateOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(99, "something went wrong", null, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Success", String.format("OTP sent to %s ", formattedEmail), "", null));
    }

    @Override
    public ResponseEntity<ApiResponse> handlePasswordOtpVerification(VerifyPasswordOtpRequest request) {
        var result = otpService.validateOtp(request.otp(), request.email(), OtpEvent.FORGOT_PASSWORD);

        if (!result.isValid()) {
            return ResponseEntity
                    .status(result.httpStatus())
                    .body(ResponseUtil.error(99, result.errorMessage(), result.details(), null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "verification successful", "otp verified", "", ""));
    }
}
