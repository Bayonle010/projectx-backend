package com.project_x.role.service.impl;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.role.RoleRepository;
import com.project_x.role.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("RoleService")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * @return 
     */
    @Override
    public ResponseEntity<ApiResponse> handleFetchRoles() {
        List<String> baseRoles = roleRepository.findAll()
                .stream()
                // remove the "ROLE_" prefix (only if present)
                .map(r -> {
                    String auth = r.getAuthority();
                    return auth.startsWith("ROLE_")
                            ? auth.substring("ROLE_".length())
                            : auth;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ResponseUtil.success(0, "Available roles fetched", "",  baseRoles, null)
        );
    }
}
