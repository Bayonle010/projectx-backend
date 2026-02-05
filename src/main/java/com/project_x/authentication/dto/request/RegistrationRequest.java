package com.project_x.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "firstName field cannot be blank")
        String firstName,

        @NotBlank(message = "LastName field cannot be blank")
        String lastName,

        @NotBlank(message = "email field cannot be blank")
        @Email(message = "invalid email format")
        String email,

        @Pattern(
        regexp = "^(\\+234|0)[789][01]\\d{8}$",
        message = "Phone number must be a valid Nigerian number")
        @NotBlank(message = "phone number cannot be empty")
        String phoneNumber,

        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()\\-_=+{}\\[\\]|;:'\",.<>?/`~])(?=\\S+$).{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
        @NotBlank(message = "password field cannot be blank")
        String password

){
}
