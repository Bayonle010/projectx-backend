package com.project_x.authentication.customauth.dto.response;

import com.project_x.user.dto.respose.UserResponse;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse userResponse
) {
}
