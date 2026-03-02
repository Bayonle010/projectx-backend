package com.project_x.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyPasswordOtpRequest(
        @NotBlank(message = "otp field is required")
        String otp,

        @NotBlank(message = "email field is required")
        @Email(message = "input a valid email address")
        String email
) {
}
