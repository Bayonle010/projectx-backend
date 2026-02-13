package com.project_x.authentication.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse userResponse
) {
}
