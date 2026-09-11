package com.project_x.role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTypeTests {

    @Test
    void exposesAuthoritiesThatMatchTheSeededRoleNames() {
        assertEquals("ROLE_USER", RoleType.USER.getAuthority());
        assertEquals("ROLE_ADMIN", RoleType.ADMIN.getAuthority());
        assertEquals("ROLE_SUPER_ADMIN", RoleType.SUPER_ADMIN.getAuthority());
    }
}
