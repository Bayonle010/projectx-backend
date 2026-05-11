package com.project_x.user.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.user.dto.respose.UserResponse;
import com.project_x.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse> fetchAuthenticatedUser(@RequestAttribute("AUTH_IDENTITY")AuthenticationIdentity authenticationIdentity){
        UserResponse userResponse = userService.fetchPublicInfoForAuthenticatedUser(authenticationIdentity);
        return ResponseEntity.ok(ResponseUtil.success(0, "user details fetched successfully", "", userResponse, ""));
    }
}
