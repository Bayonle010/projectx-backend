package com.project_x.authentication.builder;

import com.project_x.authentication.dto.response.UserResponse;
import com.project_x.role.Role;
import com.project_x.user.entity.User;

import java.util.List;

public class UserResponseBuilder {
    public static UserResponse toDto(User user){
        List<String> roles = user.getRoles().stream().map(Role::getAuthority).toList();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstname())
                .lastName(user.getLastname())
                .isVerified(user.isEmailVerified())
                .role(String.valueOf(roles))
                .userType(String.valueOf(user.getUserType()))
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
