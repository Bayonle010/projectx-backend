package com.project_x.user.dto.respose;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record UserResponse (
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean isEmailVerified,
        String userType
){
}
