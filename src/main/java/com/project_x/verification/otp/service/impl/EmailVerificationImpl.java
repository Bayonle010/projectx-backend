package com.project_x.verification.otp.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.user.enums.UserType;
import com.project_x.user.service.UserService;
import com.project_x.verification.otp.dto.request.SignUpOtpRequest;
import com.project_x.verification.otp.enums.OtpEvent;
import com.project_x.verification.otp.service.EmailVerification;
import com.project_x.verification.otp.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class EmailVerificationImpl implements EmailVerification {
    private final UserService userService;
    private final OtpService otpService;

    public EmailVerificationImpl(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @Override
    public ResponseEntity<ApiResponse> generateSignUpOtp(SignUpOtpRequest signUpOtpRequest) {
        String formattedEmail = signUpOtpRequest.email().toLowerCase().trim();

        userService.findUserByEmail(formattedEmail);

        boolean generateOtp = otpService.handleGenerateOtp(
                signUpOtpRequest.email().toLowerCase().trim(), OtpEvent.SIGN_UP, 300L, "EMAIL VERIFICATION",
                UserType.USER, "otp"
        );

        if (!generateOtp) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtil.error(99, "something went wrong", null, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtil.success(0, "Success", String.format("OTP sent to %s ", formattedEmail), "",null));
    }
}
