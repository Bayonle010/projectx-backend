package com.project_x.verification.otp.dto.request;

import com.project_x.verification.otp.enums.OtpEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record VerifyOtpEventRequest(
        @NotBlank(message = "otp value cannot be empty")
        String otp,

        @NotBlank(message = "otp medium is required")
        String otpMedium,

        @NotNull(message = "otpEvent Field Cannot Be Empty")
        OtpEvent otpEvent
) {
}
