package com.project_x.verification.otp.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank(message = "otp cannot be blank")
        String otp,

        @NotBlank(message = "otp medium cannot be blank")
         String email
) {
}
