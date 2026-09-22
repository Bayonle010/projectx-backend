package com.project_x.role.service.impl;

import com.project_x.role.Role;
import com.project_x.role.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTests {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void findsTheRequestedAuthorityInsteadOfAlwaysReturningTheUserRole() {
        Role adminRole = new Role("ROLE_ADMIN");
        when(roleRepository.findByAuthority("ROLE_ADMIN"))
                .thenReturn(Optional.of(adminRole));

        Optional<Role> result = roleService.findByAuthority("ROLE_ADMIN");

        assertSame(adminRole, result.orElseThrow());
        verify(roleRepository).findByAuthority("ROLE_ADMIN");
    }
}
