package com.project_x.core.security.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationIdentity {
    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String userType;
}
