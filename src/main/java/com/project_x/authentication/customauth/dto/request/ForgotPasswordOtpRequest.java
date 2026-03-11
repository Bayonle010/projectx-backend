package com.project_x.authentication.customauth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record ForgotPasswordOtpRequest(
        @NotBlank(message = "email field is required")
        @Email(message = "invalid email format")
        String email
) {
}
