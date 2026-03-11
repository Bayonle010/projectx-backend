package com.project_x.authentication.customauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
        @NotBlank(message = "refreshtoken field cannot be blank")
        @NotNull(message = "refreshtoken fields cannot be null")
        String refreshToken
) {
}
