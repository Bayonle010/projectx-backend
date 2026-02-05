package com.project_x.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "email field cannot be blank")
        @NotNull(message = "email field cannot be null")
        @Email(message = "invalid email format")
        String email,

        @NotNull(message = "email field cannot be null")
        @NotBlank(message = "password field cannot be blank")
        String password
) {
}
