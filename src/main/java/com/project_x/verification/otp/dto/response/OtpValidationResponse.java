package com.project_x.verification.otp.dto.response;

import com.project_x.verification.otp.entity.Otp;
import org.springframework.http.HttpStatus;

public record OtpValidationResponse(
        boolean isValid,
        String errorMessage,
        String details,
        HttpStatus httpStatus,
        Otp otp
) {

    public static OtpValidationResponse success(Otp otp) {
        return new OtpValidationResponse(
                true,
                null,
                null,
                HttpStatus.OK,
                otp
        );
    }

    public static OtpValidationResponse failure(HttpStatus status, String message, String details) {
        return new OtpValidationResponse(
                false,
                message,
                details,
                status,
                null
        );
    }
}
