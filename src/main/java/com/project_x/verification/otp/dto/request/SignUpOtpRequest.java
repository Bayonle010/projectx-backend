package com.project_x.verification.otp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpOtpRequest(
        @NotBlank(message = "email cannot be empty")
        @Email(message = "invalid email format")
        String email
) {
}

