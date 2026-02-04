package com.project_x.role.service;

import com.project_x.core.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface RoleService {
    ResponseEntity<ApiResponse> handleFetchRoles();
}
