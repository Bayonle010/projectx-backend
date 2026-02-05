package com.project_x.authentication.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse (
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String role,
        boolean isVerified,
        String userType
){
}
