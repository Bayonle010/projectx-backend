package com.project_x.role;

import lombok.Getter;

@Getter
public enum RoleType {
    HOUSER_SEEKER("ROLE_HOUSE_SEEKER"),
    HOUSER_OWNER("ROLE_HOUSE_SEEKER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");


    private final String authority;
    RoleType(String authority) { this.authority = authority; }

}