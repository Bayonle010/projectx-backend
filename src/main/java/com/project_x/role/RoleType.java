package com.project_x.role;

import lombok.Getter;

@Getter
public enum RoleType {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN");


    private final String authority;
    RoleType(String authority) { this.authority = authority; }

}
