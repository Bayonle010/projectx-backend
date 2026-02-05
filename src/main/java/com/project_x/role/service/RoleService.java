package com.project_x.role.service;

import com.project_x.core.response.ApiResponse;
import com.project_x.role.Role;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface RoleService {
    ResponseEntity<ApiResponse> handleFetchRoles();
    Optional<Role> findByAuthority(String role);
}
